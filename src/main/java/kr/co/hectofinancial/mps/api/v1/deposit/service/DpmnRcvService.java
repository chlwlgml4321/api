package kr.co.hectofinancial.mps.api.v1.deposit.service;

import kr.co.hectofinancial.mps.api.v1.deposit.domain.DpmnRcv;
import kr.co.hectofinancial.mps.api.v1.deposit.domain.DpmnRcvDtl;
import kr.co.hectofinancial.mps.api.v1.deposit.repository.DpmnRcvDtlRepository;
import kr.co.hectofinancial.mps.api.v1.deposit.repository.DpmnRcvRepository;
import kr.co.hectofinancial.mps.api.v1.market.repository.MarketServiceProductRepository;
import kr.co.hectofinancial.mps.api.v1.market.repository.MpsMarketRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.DpStatCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DpmnRcvService {
    private final DpmnRcvRepository dpmnRcvRepository;
    private final DpmnRcvDtlRepository dpmnRcvDtlRepository;
    private final MpsMarketRepository mpsMarketRepository;
    private final MarketServiceProductRepository marketServiceProductRepository;
    private final DstbDpmnRcvService dstbDpmnRcvService;

    @Transactional(rollbackFor = Exception.class)
    public void saveDpAmt(String mid, long dpAmt, String globalId) {

        /* 동시성 제어를 위한 쿼리 실행 */
        mpsMarketRepository.lockRowByMid(mid);
        String dstbMId = marketServiceProductRepository.lockRowByMid(mid);

        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();

        //선불 상품권 예치금인지 판단
        if(dstbMId != null){
            log.info("예치금 입금처리 => 선불상품권거래 인입 :[{}]", globalId);
            dstbDpmnRcvService.saveDpAmt(dstbMId, curDt, dpAmt, globalId);
            return;
        }

        long depositToAdd = 0;
        DpmnRcv lastDpmnRcv = null;
        long totDpAmt = dpAmt;
        List<DpmnRcvDtl> orgDpmnRcvDtl = dpmnRcvDtlRepository.findByMidAndDpNotiNo(mid, globalId);

        if(orgDpmnRcvDtl.size() > 0){
            log.info("---------- 기처리거래 END globalId: [{}], 금액: [{}] ----------", globalId, totDpAmt);
        } else if(orgDpmnRcvDtl.size() == 0){
            List<DpmnRcv> tgtDpmnRcvList = dpmnRcvRepository.findByMidAndDpStatCdOrderByTrdDt(mid, DpStatCd.UNDER_PAYMENT.getDpStatCd());
            if(tgtDpmnRcvList.size() == 0 && dpAmt == 0){
                log.info("---------- 입급 처리 거래 없음 END ----------");
            }else if(tgtDpmnRcvList.size() == 0 && dpAmt > 0) { // 미납거래없을때

                DpmnRcv overDpmnRcv = dpmnRcvRepository.findByDpStatCdOrderByUpdateDtDesc(mid, DpStatCd.FULL_PAYMENT.getDpStatCd(), DpStatCd.OVER_PAYMENT.getDpStatCd());
                if(overDpmnRcv != null){
                    overDpmnRcv.setDpExcsAmt(overDpmnRcv.getDpExcsAmt() + dpAmt);
                    overDpmnRcv.orgDpmnRcdUpdate();
                    overDpmnRcv.setDpStatCd(DpStatCd.OVER_PAYMENT.getDpStatCd());
                    dpmnRcvRepository.save(overDpmnRcv);

                    /* 선불예치금 상세 SET */
                    dpmnRcvDtlRepository.save(DpmnRcvDtl.builder()
                            .trdDt(overDpmnRcv.getTrdDt())
                            .mid(mid)
                            .dpNotiNo(globalId)
                            .dpDt(curDt)
                            .dpAmt(dpAmt)
                            .createdIp(ServerInfoConfig.HOST_IP)
                            .createdId(ServerInfoConfig.HOST_NAME)
                            .createdDate(LocalDateTime.now())
                            .build());
                }else{
                    log.error("선불예치금 입금처리 저장 오류 M_ID : [{}], 사유: [{}]", mid, "MPS.TB_MPS_DPMN_RCV 생성전에 입금처리됨");

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("(MPS_API) 선불 예치금 입금 데이터 저장 오류")
                            .append("\n")
                            .append("======================")
                            .append("\n")
                            .append("입금 일자: " + curDt)
                            .append("\n")
                            .append("M_ID: " + mid)
                            .append("\n")
                            .append("사유: " + "MPS.TB_MPS_DPMN_RCV 생성전에 입금처리됨")
                            .append("\n")
                            .append("globalId: " + globalId)
                            .append("\n")
                            .append("======================");
                    MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), stringBuilder.toString());
                }

            }else if(tgtDpmnRcvList.size() >0 && dpAmt > 0){ // 미납거래가있을 때
                for (DpmnRcv list : tgtDpmnRcvList) {
                    long dpReqAmt = list.getDpReqAmt() - list.getDpAmt();

                    if (dpReqAmt > 0) {
                        // 입금할 금액 계산
                        depositToAdd = Math.min(dpReqAmt, dpAmt);
                        list.setDpAmt(list.getDpAmt() + depositToAdd);
                        list.orgDpmnRcdUpdate();

                        dpAmt -= depositToAdd;
                        lastDpmnRcv = list;

                        if (list.getDpReqAmt() == list.getDpAmt()) {
                            list.setDpStatCd(DpStatCd.FULL_PAYMENT.getDpStatCd());
                            list.orgDpmnRcdUpdate();
                        } else if (dpReqAmt > depositToAdd) {
                            list.setDpStatCd(DpStatCd.UNDER_PAYMENT.getDpStatCd());
                            list.orgDpmnRcdUpdate();
                        }

                        /* 선불예치금 상세 SET */
                        dpmnRcvDtlRepository.save(DpmnRcvDtl.builder()
                                .trdDt(list.getTrdDt())
                                .mid(mid)
                                .dpNotiNo(globalId)
                                .dpDt(curDt)
                                .dpAmt(depositToAdd)
                                .createdIp(ServerInfoConfig.HOST_IP)
                                .createdId(ServerInfoConfig.HOST_NAME)
                                .createdDate(LocalDateTime.now())
                                .build());
                    }
                    if (dpAmt <= 0) {
                        break;
                    }
                }

                if (dpAmt > 0 && lastDpmnRcv != null) {
                    lastDpmnRcv.setDpExcsAmt(lastDpmnRcv.getDpExcsAmt()+ dpAmt); // 초과 금액 추가
                    lastDpmnRcv.setDpStatCd(DpStatCd.OVER_PAYMENT.getDpStatCd());
                    lastDpmnRcv.orgDpmnRcdUpdate();

                    DpmnRcvDtl tgtDpmnRcvDtl = dpmnRcvDtlRepository.findTopByMidAndDpNotiNoAndDpAmtOrderByModifiedDateDesc(mid, globalId, depositToAdd);
                    // tgtDpmnRcvDtl 직전에 SET 한 데이터를 참조하지만 null 가능성을 고려해 예외처리 2025-06-19
                    if(tgtDpmnRcvDtl != null){
                        tgtDpmnRcvDtl.setDpAmt(tgtDpmnRcvDtl.getDpAmt() + dpAmt);
                        dpmnRcvDtlRepository.save(tgtDpmnRcvDtl);

                        log.info("!!선불예치금 수납 초과 처리!! M_ID : [{}]", mid);
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("(MPS_API) 선불 예치금 수납 초과 발생")
                                .append("\n")
                                .append("======================")
                                .append("\n")
                                .append("입금 일자: " + curDt)
                                .append("\n")
                                .append("M_ID: " + mid)
                                .append("\n")
                                .append("globalId: " + globalId)
                                .append("\n")
                                .append("======================");
                        MonitAgent.sendMonitForBiz("BIZS-02", stringBuilder.toString());
                    }else{
                        log.error("선불예치금 초과처리 저장 오류 M_ID : [{}]", mid);
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("(MPS_API) 선불 예치금 수납 초과 데이터 저장 오류")
                                .append("\n")
                                .append("======================")
                                .append("\n")
                                .append("입금 일자: " + curDt)
                                .append("\n")
                                .append("M_ID: " + mid)
                                .append("\n")
                                .append("globalId: " + globalId)
                                .append("\n")
                                .append("======================");
                        MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), stringBuilder.toString());
                    }
                }
                dpmnRcvRepository.saveAll(tgtDpmnRcvList);
            }
            // tgtDpmnRcvDtl 직전에 SET 한 데이터를 참조하지만 null 가능성을 고려해 예외처리 2025-06-19
            DpmnRcvDtl dpmnRcvDtl = dpmnRcvDtlRepository.findTopByMidAndDpNotiNoOrderByTrdDt(mid, globalId);
            if(dpmnRcvDtl != null){
                dpmnRcvRepository.updateAcmAmt(mid, dpmnRcvDtl.getTrdDt());
            }else{
                log.error("선불예치금 누적금액 저장 오류 M_ID : [{}]", mid);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("(MPS_API) 선불 예치금 누적금액 데이터 저장 오류")
                        .append("\n")
                        .append("======================")
                        .append("\n")
                        .append("입금 일자: " + curDt)
                        .append("\n")
                        .append("M_ID: " + mid)
                        .append("\n")
                        .append("globalId: " + globalId)
                        .append("\n")
                        .append("======================");
                MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), stringBuilder.toString());
            }
            log.info("---------- 입급 처리 거래 완료 M_ID: [{}], GLOBAL_ID: [{}], 처리금액: [{}]----------", mid, globalId, totDpAmt);
        }
    }

}
