package kr.co.hectofinancial.mps.api.v1.csrc.service;


import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.cpn.service.CpnService;
import kr.co.hectofinancial.mps.api.v1.csrc.domain.EzpCsrcIss;
import kr.co.hectofinancial.mps.api.v1.csrc.dto.CashRcptRegistResponseDto;
import kr.co.hectofinancial.mps.api.v1.csrc.dto.CashRcptResistRequestDto;
import kr.co.hectofinancial.mps.api.v1.csrc.repository.EzpCsrIssRepository;
import kr.co.hectofinancial.mps.api.v1.customer.domain.Customer;
import kr.co.hectofinancial.mps.api.v1.customer.repository.CustomerRepository;
import kr.co.hectofinancial.mps.api.v1.trade.domain.PayMny;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.domain.UseMny;
import kr.co.hectofinancial.mps.api.v1.trade.repository.PayMnyRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.UseMnyRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.CsrcIssCd;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashRcptService {

    @Value("${spring.profiles.active}")
    private String profiles;
    private final TradeRepository tradeRepository;
    private final UseMnyRepository useMnyRepository;
    private final PayMnyRepository payMnyRepository;
    private final CustomerRepository customerRepository;
    private final SequenceService sequenceService;
    private final EzpCsrIssRepository ezpCsrIssRepository;
    private final CpnService cpnService;

    @Transactional(rollbackFor = Exception.class)
    public CashRcptRegistResponseDto cashRcptResist(CashRcptResistRequestDto cashRcptResistRequestDto) throws Exception {

        int totCnt = 0;
        int sucCnt = 0;
        String purpose = "";
        String identityGb = "";
        long splAmt = 0;
        double vat = 0;
        Customer tgtCustomer;
        String decCsrcRegNo = "";
        PayMny payMny;
        List<String> csrcIssStatCdList;
        String curDt = DateTimeUtil.getCurDtim("yyyyMMdd");
        String curTm = DateTimeUtil.getCurDtim("HHmmss");
        long trdAmt = 0;

        csrcIssStatCdList = new ArrayList<>(Arrays.asList(CsrcIssCd.RCPT_SUCCESS.getCsrcIssStatCd(), CsrcIssCd.ISSUE_Y.getCsrcIssStatCd()));
        List<Trade> cuTradeList = tradeRepository.findByMpsCustNoAndTrdDivCdAndTrdDtAndCsrcIssReqYnAndCsrcIssStatCdNotIn(cashRcptResistRequestDto.getCustNo(), TrdDivCd.COMMON_USE.getTrdDivCd(), curDt, "Y", csrcIssStatCdList);

        totCnt = cuTradeList.size();
        if (totCnt > 0) {
            for (Trade list : cuTradeList) {
                trdAmt = 0;

                /* 머니 사용원장 조회 */
                List<UseMny> useMnyList = useMnyRepository.findByUseTrdNo(list.getTrdNo());

                for(UseMny usList : useMnyList) {
                    trdAmt += usList.getUseAmt();
                }
                if(list.getTrdAmt() == list.getCnclTrdAmt() || list.getMnyAmt() == list.getCnclMnyAmt()){
                    trdAmt = 0;
                    list.setCsrcIssStatCd(CsrcIssCd.ORG_TRADE_CANCEL.getCsrcIssStatCd());
                    log.info("현금영수증 발급 제외 머니금액 전체 취소 완료: [{}]", list.getTrdNo());
                }

                if (trdAmt > 0) {
                    log.info("현금영수증 접수 금액: [{}]", trdAmt);
                    tgtCustomer = customerRepository.findCustomerByMpsCustNo(list.getMpsCustNo()).orElse(null);

                    if (tgtCustomer == null || tgtCustomer.getCsrcRegNoDivCd() == null || tgtCustomer.getCsrcRegNoEnc() == null) {
                        log.error("회원 현금영수증 등록정보 없음: [{}]", list.getTrdNo());
                    } else {
                        purpose = tgtCustomer.getCsrcRegNoDivCd().substring(0, 1);
                        identityGb = tgtCustomer.getCsrcRegNoDivCd().substring(1);
                        /* 복호화 */
                        try {
                            DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
                            decCsrcRegNo = databaseAESCryptoUtil.convertToEntityAttribute(tgtCustomer.getCsrcRegNoEnc());
                            log.info("decCsrcRegNo: [{}], 거래번호: [{}]", decCsrcRegNo, list.getTrdNo());
                        } catch (Exception e) {
                            log.error("--- 복호화에러 거래번호: [{}]---", list.getTrdNo());
                        }

                        String issReqNo = sequenceService.generateCsrcTradeSeq01(); //현금영수증 시퀀스
                        String cpnId = cpnService.getCpnId(list.getStlMId());
                        String bizRegno = cpnService.getCpn(cpnId).getBizRegNo();

                        if (profiles.equals("local") || profiles.equals("test")) {
                            bizRegno = "1234567890";
                        }

                        /* 부가세 */
                        vat = (trdAmt * 10) / 110;
                        /* 공급가 */
                        splAmt = trdAmt - Math.round(vat);
                        try {
                            /* 현금영수증 접수 SET */
                            EzpCsrcIss ezpCsrcIss = EzpCsrcIss.builder()
                                    .issReqNo(issReqNo)
                                    .trdNo(list.getTrdNo())
                                    .trdDt(list.getTrdDt())
                                    .trdTm(list.getTrdTm())
                                    .mid(list.getStlMId())
                                    .reqDtm(curDt + curTm)
                                    .cnclYn("N")
                                    .trdAmt(trdAmt)
                                    .splAmt(splAmt)
                                    .vat(Math.round(vat))
                                    .svcAmt(0)
                                    .taxTypeCd("N")
                                    .bizRegNo(bizRegno)
                                    .outStatCd("0000") //고정값
                                    .mtrdNo(list.getMTrdNo())
                                    .custNm(tgtCustomer.getCustNm())
                                    .issPurpsDivCd(purpose)
                                    .csrcRegNoDivCd(identityGb)
                                    .csrcRegNoEnc(tgtCustomer.getCsrcRegNoEnc())
                                    .svcCd(list.getSvcCd())
                                    .prdtCd(list.getPrdtCd())
                                    .createdDate(DateTimeUtil.convertStringToLocalDateTime(new CustomDateTimeUtil().getDateTime()))
                                    .createdIp(ServerInfoConfig.HOST_IP)
                                    .createdId(ServerInfoConfig.HOST_NAME)
                                    .build();
                            ezpCsrIssRepository.save(ezpCsrcIss);

                            //거래테이블 set
                            sucCnt++;
                            list.setCsrcIssStatCd(CsrcIssCd.RCPT_SUCCESS.getCsrcIssStatCd());
                            list.setCsrcApprDtm(curDt + curTm);
                            list.setCsrcApprNo(issReqNo);
                            list.setModifiedDate(LocalDateTime.now());
                            list.setModifiedId(ServerInfoConfig.HOST_NAME);
                            list.setModifiedIp(ServerInfoConfig.HOST_IP);
                            tradeRepository.save(list);
                        } catch (Exception e) {
                            log.info("--- 현금영수증 등록 ERROR --- 거래번호: [{}], 오류: [{}]", list.getTrdNo(), e.getMessage());
                            list.setCsrcIssStatCd(CsrcIssCd.RCPT_FAIL.getCsrcIssStatCd());
                            list.setModifiedDate(LocalDateTime.now());
                            list.setModifiedId(ServerInfoConfig.HOST_NAME);
                            list.setModifiedIp(ServerInfoConfig.HOST_IP);
                            tradeRepository.save(list);
                        }
                    }
                }

            }
        }
        log.info("총 건수: [{}], 처리 건수: [{}]", totCnt, sucCnt);
        return CashRcptRegistResponseDto.builder()
                .custNo(cashRcptResistRequestDto.getCustNo())
                .totCnt(totCnt)
                .sucCnt(sucCnt)
                .build();
    }

}
