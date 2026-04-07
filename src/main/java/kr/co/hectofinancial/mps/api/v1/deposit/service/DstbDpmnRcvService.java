package kr.co.hectofinancial.mps.api.v1.deposit.service;

import kr.co.hectofinancial.mps.api.v1.deposit.domain.DstbDpmnRcv;
import kr.co.hectofinancial.mps.api.v1.deposit.domain.DstbDpmnRcvDtl;
import kr.co.hectofinancial.mps.api.v1.deposit.repository.DstbDpmnRcvDtlRepository;
import kr.co.hectofinancial.mps.api.v1.deposit.repository.DstbDpmnRcvRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.DpStatCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DstbDpmnRcvService {
    private final DstbDpmnRcvRepository dstbDpmnRcvRepository;
    private final DstbDpmnRcvDtlRepository dstbDpmnRcvDtlRepository;

    @Transactional
    public void saveDpAmt(String mid, String trdDt, long dpAmt, String globalId){

        long depositToAdd = 0;
        DstbDpmnRcv lastDstbDpmnRcv = null;
        long totDpAmt = dpAmt;

        List<DstbDpmnRcvDtl> orgDstbDpmnRcvDtl = dstbDpmnRcvDtlRepository.findByMidAndDpNotiNo(mid, globalId);
        if(orgDstbDpmnRcvDtl.size() > 0){
            log.info("---------- 기처리거래 END globalId: [{}], 금액: [{}] ----------", globalId, totDpAmt);
        } else if(orgDstbDpmnRcvDtl.size() == 0){
            List<DstbDpmnRcv> tgtDstbDpmnRcvList = dstbDpmnRcvRepository.findByMidAndDpStatCdOrderByTrdDt(mid, DpStatCd.UNDER_PAYMENT.getDpStatCd());
            if(tgtDstbDpmnRcvList.size() == 0 && dpAmt == 0){
                log.info("---------- 입급 처리 거래 없음 END ----------");
            }else if(tgtDstbDpmnRcvList.size() == 0 && dpAmt > 0) { // 미납거래없을때

                DstbDpmnRcv overDstbDpmnRcv = dstbDpmnRcvRepository.findByDpStatCdOrderByUpdateDtDesc(mid, DpStatCd.FULL_PAYMENT.getDpStatCd(), DpStatCd.OVER_PAYMENT.getDpStatCd());
                if(overDstbDpmnRcv != null){
                    overDstbDpmnRcv.setDpExcsAmt(overDstbDpmnRcv.getDpExcsAmt() + dpAmt);
                    overDstbDpmnRcv.setDpStatCd(DpStatCd.OVER_PAYMENT.getDpStatCd());
                    overDstbDpmnRcv.orgDpmnRcdUpdate();
                    dstbDpmnRcvRepository.save(overDstbDpmnRcv);

                    /* 선불예치금 상세 SET */
                    dstbDpmnRcvDtlRepository.save(DstbDpmnRcvDtl.builder()
                            .trdDt(overDstbDpmnRcv.getTrdDt())
                            .mid(mid)
                            .dpNotiNo(globalId)
                            .dpDt(trdDt)
                            .dpAmt(dpAmt)
                            .createdIp(ServerInfoConfig.HOST_IP)
                            .createdId(ServerInfoConfig.HOST_NAME)
                            .createdDate(LocalDateTime.now())
                            .build());
                }else{
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("(MPS_API) 선불상품권 예치금 입금 데이터 저장 오류")
                            .append("\n")
                            .append("======================")
                            .append("\n")
                            .append("M_ID: " + mid)
                            .append("\n")
                            .append("사유: " + "MPS.TB_DSTB_DPMN_RCV 생성전에 입금처리됨")
                            .append("\n")
                            .append("======================");
                    MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), stringBuilder.toString());
                }

            } else if (tgtDstbDpmnRcvList.size() > 0 && dpAmt > 0) { // 미납거래가있을 때

                for (DstbDpmnRcv list : tgtDstbDpmnRcvList) {
                    long dpReqAmt = list.getDpReqAmt() - list.getDpAmt();

                    if (dpReqAmt > 0) {

                        // 입금할 금액 계산
                        depositToAdd = Math.min(dpReqAmt, dpAmt);
                        list.setDpAmt(list.getDpAmt() + depositToAdd);
                        list.orgDpmnRcdUpdate();

                        dpAmt -= depositToAdd;
                        lastDstbDpmnRcv = list;
                        if (list.getDpReqAmt() == list.getDpAmt()) {
                            list.setDpStatCd(DpStatCd.FULL_PAYMENT.getDpStatCd());
                            list.orgDpmnRcdUpdate();
                        } else if (dpReqAmt > depositToAdd) {
                            list.setDpStatCd(DpStatCd.UNDER_PAYMENT.getDpStatCd());
                            list.orgDpmnRcdUpdate();
                        }

                        /* 선불예치금 상세 SET */
                        dstbDpmnRcvDtlRepository.save(DstbDpmnRcvDtl.builder()
                                .trdDt(list.getTrdDt())
                                .mid(mid)
                                .dpNotiNo(globalId)
                                .dpDt(trdDt)
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

                if (dpAmt > 0 && lastDstbDpmnRcv != null) {
                    lastDstbDpmnRcv.setDpExcsAmt(lastDstbDpmnRcv.getDpExcsAmt() + dpAmt); // 초과 금액 추가
                    lastDstbDpmnRcv.setDpStatCd(DpStatCd.OVER_PAYMENT.getDpStatCd());
                    lastDstbDpmnRcv.orgDpmnRcdUpdate();

                    DstbDpmnRcvDtl tgtDstbDpmnRcvDtl = dstbDpmnRcvDtlRepository.findTopByMidAndDpNotiNoAndDpAmtOrderByModifiedDateDesc(mid, globalId, depositToAdd);

                    // tgtDstbDpmnRcvDtl 직전에 SET 한 데이터를 참조하지만 null 가능성을 고려해 예외처리 2025-06-19
                    if(tgtDstbDpmnRcvDtl != null){
                        tgtDstbDpmnRcvDtl.setDpAmt(tgtDstbDpmnRcvDtl.getDpAmt() + dpAmt);
                        dstbDpmnRcvDtlRepository.save(tgtDstbDpmnRcvDtl);

                        String message = String.format("{BAT} 선불상품권 예치금 수납 초과처리!! M_ID: " + mid);
                        MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
                    }else{
                        log.error("선불상품권 예치금 초과처리 저장 오류 M_ID : [{}]", mid);
                        String message = String.format("**ERROR 발생** 선불상품권 예치금 초과처리 저장 오류 M_ID: " + mid);
                        MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
                    }
                }
                dstbDpmnRcvRepository.saveAll(tgtDstbDpmnRcvList);
            }
            // tgtDstbDpmnRcvDtl 직전에 SET 한 데이터를 참조하지만 null 가능성을 고려해 예외처리 2025-06-19
            DstbDpmnRcvDtl dstbDpmnRcvDtl = dstbDpmnRcvDtlRepository.findTopByMidAndDpNotiNoOrderByTrdDt(mid, globalId);
            if(dstbDpmnRcvDtl != null){
                dstbDpmnRcvRepository.updateAcmAmt(mid, dstbDpmnRcvDtl.getTrdDt());
            }else{
                log.error("선불상품권 예치금 누적금액 저장 오류 M_ID : [{}]", mid);
                String message = String.format("**ERROR 발생** 선불상품권 예치금 누적금액 저장 오류 M_ID: " + mid);
                MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
            }
            log.info("---------- 입급 처리 거래 완료 M_ID: [{}], GLOBAL_ID: [{}], 처리금액: [{}]----------", mid, globalId, totDpAmt);
        }
    }
}