package kr.co.hectofinancial.mps.api.v1.firm.service;

import kr.co.hectofinancial.mps.api.v1.deposit.service.DpmnRcvService;
import kr.co.hectofinancial.mps.api.v1.firm.domain.DpmnNoti;
import kr.co.hectofinancial.mps.api.v1.firm.domain.FcrcSumry;
import kr.co.hectofinancial.mps.api.v1.firm.dto.FirmDepositeNoticeRequestDto;
import kr.co.hectofinancial.mps.api.v1.firm.dto.RemittanceApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.firm.dto.RemittanceResultDto;
import kr.co.hectofinancial.mps.api.v1.firm.repository.DpmnNotiRepository;
import kr.co.hectofinancial.mps.api.v1.firm.repository.FcrcSumryRepository;
import kr.co.hectofinancial.mps.api.v1.trade.service.money.RfdRsltCnfInsertService;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.MpsApiCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import kr.co.hectofinancial.mps.global.util.SeedUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DpmnNotiService {

    @Value("${firm.vanIsttCd}")
    private String firmVanIsttCd;
    @Value("${firm.scMid}")
    private String scMid;
    @Value("${rmt.mid}")
    private String rfdMid;
    @Value("${seed.userKey}")
    private String userKey;
    @Value("${seed.IV}")
    private String IV;
    private final DpmnNotiRepository dpmnNotiRepository;
    private final DpmnRcvService dpmnRcvService;
    private final RfdRsltCnfInsertService rfdRsltCnfInsertService;
    private final FirmBankingService firmBankingService;
    private final FcrcSumryRepository fcrcSumryRepository;

    /* 입금거래명세 통지처리 */
    @Transactional(rollbackFor = Exception.class)
    public String depositeNotice(FirmDepositeNoticeRequestDto firmDepositeNoticeRequestDto) throws Exception {

        log.info("명세통지 ENC_DATA : [{}]", firmDepositeNoticeRequestDto.getData());
        String trdSumry;
        String trdDt;
        String trdTm;
        String respData;
        String encData = firmDepositeNoticeRequestDto.getData();
        String decData;
        String encRespData;
        String respCd;
        String mid = null;

        encData = encData.substring(4);
        try {
            decData = SeedUtil.decrypt(encData, userKey, IV);
//            log.info("명세통지 DEC_DATA : [{}]", decData);
        } catch (Exception e) {
            throw new RequestValidationException(ErrorCode.DECRYPT_ERROR);
        }


        /* 응답 데이터 SET */
        respData = decData.substring(0, 24) + "4100" + decData.substring(28);

        /* 거래적요 검증 */
        trdSumry = CommonUtil.cutString(decData, 179, 191, Charset.forName("EUC-KR"));
        String bankCd = decData.substring(21, 24);
        log.info("은행코드: [{}], 거래적요: [{}]", bankCd, trdSumry);
        // TODO 전각을 반각으로 변환
        //저장할때도 반각으로 저장해야함
        FcrcSumry tgtFcrcSumry = fcrcSumryRepository.findByAcntSumryAndVldStDateBetween(CommonUtil.nullTrim(trdSumry), bankCd, MpsApiCd.SVC_CD);
        if (tgtFcrcSumry != null) {
            mid = tgtFcrcSumry.getMid();
        }

        trdDt = CommonUtil.cutString(decData, 201, 209, Charset.forName("EUC-KR"));
        trdTm = CommonUtil.cutString(decData, 209, 215, Charset.forName("EUC-KR"));
        log.info("거래일자: [{}], 거래시간: [{}]", trdDt, trdTm);

        respCd = saveDpmnNoti(decData, mid, trdDt, trdTm, trdSumry);
        log.info("응답코드:"+respCd );

        respData = respData.substring(0, 52) + respCd + respData.substring(56);
        try {
            encRespData = SeedUtil.encrypt(respData, userKey, IV);
        } catch (Exception e) {
            throw new RequestValidationException(ErrorCode.ENCRYPT_ERROR);
        }
        String respHeader = "0" + encRespData.length();
        log.info("명세통지 RESP ENC_DATA: [{}]", respHeader + encRespData);
        return respHeader + encRespData;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String saveDpmnNoti(String decData, String mid, String trdDt, String trdTm, String trdSumry) throws Exception {
        DpmnNoti orgDpmnNoti;
        String globalId;
        String ornPktDivCd;
        String ornJobDivCd;
        String rmk;
        String bankCd;
        String vanIsttCd;
        String notiClssCd;
        String trdAmt;
        String amtSign;
        String pktNo;
        String reqDt; //전송일자
        String respCd = "0000"; //명세통지전문에 보내는 응답코드
        String saveResultCd = "0000";
        String outStatCd = "0021"; //원장처리코드
        String result;
        String chrgTrdNo = null;

        vanIsttCd = CommonUtil.nullTrim(decData.substring(9, 21));
        log.info("업체코드: [{}]", vanIsttCd);
//        if(!firmVanIsttCd.equals(vanIsttCd)){respCd = "0001"; outStatCd = "0031";}

        bankCd = decData.substring(21, 24);
        ornPktDivCd = decData.substring(24, 28);
        ornJobDivCd = decData.substring(28, 31);
        if(!ornPktDivCd.equals("4000")){respCd = "0001"; outStatCd = "0031";}
        if(!ornJobDivCd.equals("100")){respCd = "0001"; outStatCd = "0031";}

        notiClssCd = decData.substring(122, 124);
        log.info("입/출금 구분코드: [{}]", notiClssCd);
        pktNo = decData.substring(32, 38); //전문번호
        log.info("은행코드: [{}], 전문번호: [{}]", bankCd, pktNo);
        reqDt = decData.substring(38, 46);

        trdAmt = CommonUtil.tgRemoveStr(decData.substring(126, 139), "0");
        amtSign = decData.substring(165, 166);

        //고정값("PP_") + 업체번호(vanIsttCd) + bankCd + pktDivCd + 전문번호 + 전송일자
        globalId = "PP_" +  vanIsttCd + bankCd + decData.substring(24, 28) + pktNo + reqDt;

        log.info("globalId :[{}]", globalId);
        if (mid == null) {
            saveResultCd = "0001";
            outStatCd = "0031";

            if(notiClssCd.equals("11")){
                log.error("!!명세통지 거래적요 ERROR!! M_ID IS NULL !! globalId: [{}], ACNT_SUMRY: [{}]", globalId, trdSumry);
                MonitAgent.sendMonitForBiz("BIZS-01", "명세통지 거래적요 ERROR M_ID IS NULL [globalId: " + globalId + ", 거래적요: " + trdSumry + "]");
            }
        }

        if("023".equals(bankCd)){
            // 정상 처리된 명세통지
            orgDpmnNoti = dpmnNotiRepository.findByGlobalIdAndTrdDtAndOutRsltCdAndSvcCdAndVanIsttCd(globalId, trdDt, "0000", MpsApiCd.SVC_CD, vanIsttCd);

            //이미 결번으로 들어와있는 경우 저장x todo 결번에서 처리안됐을경우
            DpmnNoti nDpmnNoti = dpmnNotiRepository.findBySvcCdAndVanIsttCdAndOrnIdAndOrnPktNoAndOrnPktDivCdAndOrnJobDivCdAndTrdDtAndTrdTmAndTrdAmtAndNotiClssCd(MpsApiCd.SVC_CD, vanIsttCd, bankCd, pktNo, "4100", "200", trdDt, trdTm, Long.parseLong(trdAmt), notiClssCd);

            if (orgDpmnNoti == null && nDpmnNoti == null) {
                String encMacntAccNoDp; //명세통지 모계좌
                String mskMacntAccNo;
                String accNo = decData.substring(100, 115);

                rmk = "계좌잔액:" + amtSign + CommonUtil.tgRemoveStr(decData.substring(166, 179), "0") + ";";

                /* 모계좌 정보 등록 */
                try {
                    DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
                    encMacntAccNoDp = databaseAESCryptoUtil.convertToDatabaseColumn(accNo);
                    log.info("모계좌 계좌번호 암호화: [{}]", encMacntAccNoDp);
                } catch (Exception e) {
                    throw new RequestValidationException(ErrorCode.ENCRYPT_ERROR);
                }
                mskMacntAccNo = CommonUtil.maskingString(accNo, 3, 3);

                try {
                    dpmnNotiRepository.save(DpmnNoti.builder()
                            .globalId(globalId)
                            .trdDt(trdDt)
                            .trdTm(trdTm)
                            .mid(mid)
                            .svcCd(MpsApiCd.SVC_CD)
                            .outStatCd(outStatCd)
                            .outRsltGrpCd("PYAG") //고정값
                            .outRsltCd(saveResultCd)
                            .crcCd("KRW") //고정값
                            .trdAmt(Long.parseLong(trdAmt))
                            .ornId(bankCd)
                            .ornPktDivCd(ornPktDivCd)
                            .ornJobDivCd(ornJobDivCd)
                            .ornPktNo(pktNo)
                            .ornTrdDt(trdDt)
                            .ornTrdTm(trdTm)
                            .ornSvckey(bankCd)
                            .ornRsltCd("0000") //고정값
                            .vanIsttCd(vanIsttCd)
                            .notiClssCd(notiClssCd)
                            .giroCd(CommonUtil.nullTrim(decData.substring(115, 122)))
                            .rmk(rmk)
                            .macntBankCd(bankCd)
                            .macntNoEnc(encMacntAccNoDp)
                            .macntNoMsk(mskMacntAccNo)
                            .macntSumry1(CommonUtil.nullTrim(trdSumry))
                            .createdDate(LocalDateTime.now())
                            .createdIp(ServerInfoConfig.HOST_IP)
                            .createdId(ServerInfoConfig.HOST_NAME)
                            .build());

                    String decMacntAccNo;//우리은행 모계좌 복호화
                    String encMacntAccNo;//우리은행 모계좌 암호화
                    String macBankCd;

                    /* 선불예치 입금금액 SET */
                    if (saveResultCd.equals("0000") && outStatCd.equals("0021")) {
                        if ((bankCd.equals("023") && notiClssCd.equals("11"))) {
                            if (mid != null) {

                                /* 송금 거래번호 SET 하기 위한 데이터 조회  */
                                DpmnNoti tgtDpmnNoti = dpmnNotiRepository.findByGlobalIdAndOutRsltCdAndOutStatCdAndSvcCdAndOrnIdAndOrnPktDivCdAndOrnJobDivCdAndMid(globalId, "0000", "0021", MpsApiCd.SVC_CD, bankCd, ornPktDivCd, ornJobDivCd, mid);

                                Object[] tgtMacntNo = (Object[]) dpmnNotiRepository.findByMAcntEnc(rfdMid);
                                if(tgtMacntNo == null){
                                    if(tgtDpmnNoti != null){
                                        tgtDpmnNoti.dpmnNotiFailSave();
                                    }
                                    String msg = "globalId: " + globalId + ", 금액: " + Long.parseLong(trdAmt) + "M_ID: " + mid;
                                    MonitAgent.sendMonitAgent(ErrorCode.MACNT_NO_NOT_FOUND.getErrorCode(), msg);
                                    throw new RequestValidationException(ErrorCode.MACNT_NO_NOT_FOUND);
                                }
                                macBankCd = tgtMacntNo[1].toString();
                                encMacntAccNo = tgtMacntNo[2].toString();

                                DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
                                decMacntAccNo = databaseAESCryptoUtil.convertToEntityAttribute(encMacntAccNo);

                                CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
                                String curDt = customDateTimeUtil.getDate();
                                String curTm = customDateTimeUtil.getTime();

                                RemittanceApprovalRequestDto remittanceApprovalRequestDto = RemittanceApprovalRequestDto.builder()
                                        .mchtId(scMid)
                                        .mchtTrdNo(globalId)
                                        .trdDt(curDt)
                                        .trdTm(curTm)
                                        .bankCd(macBankCd)
                                        .custAcntNo(decMacntAccNo)
                                        .custAcntSumry(trdSumry)
                                        .trdAmt(Long.parseLong(trdAmt))
                                        .macntSumry(trdSumry)
                                        .build();

                                RemittanceResultDto resultDto;
                                String rmtOutStatCd;
                                String rmtOutRsltCd = null;
                                try{
                                    resultDto = firmBankingService.remittanceApproval(remittanceApprovalRequestDto);
                                    rmtOutStatCd = resultDto.getOutStatCd();
                                    rmtOutRsltCd = resultDto.getOutRsltCd();
                                    chrgTrdNo = resultDto.getTrdNo();
                                } catch (Exception e){
                                    rmtOutStatCd = "0031";
                                    log.error(e.getMessage(), e);
                                }

                                if (!rmtOutStatCd.equals("0021")) {
                                    tgtDpmnNoti.dpmnNotiFailSave();

                                    String message = String.format("안심선불서비스 선불예치금 송금 실패 M_ID: " + mid + ", globalId: " + globalId);
                                    MonitAgent.sendMonitForBiz("BIZS-03", message);

//                                    //tODO 주석처리
//                                    /* VTIM: 타임아웃, ST38: 요청 진행 중, ST04: VAN 요청중 시스템 에러, ST06: 거래번호 정보가 없음 */ //todo 상수화 시키기
//                                    if (rmtOutRsltCd.equals("VTIM") || rmtOutRsltCd.equals("ST38") || rmtOutRsltCd.equals("ST04") || rmtOutRsltCd.equals("ST06")) {
//
//                                        mskPayAccNo = CommonUtil.maskingString(decMacntAccNo, 3, 3);
//
//                                        RfdRsltCnfInsertDto rfdRsltCnfInsertDto = RfdRsltCnfInsertDto.builder()
//                                                .trdNo(globalId)
//                                                .trdDt(curDt)
//                                                .trdTm(curTm)
//                                                .rfdTrdNo(chrgTrdNo)
//                                                .mid(mid)
//                                                .rsltCnfStatCd(RfdRsltCnfStatCd.RETRY.getRsltCnfStatCd())
//                                                .trdAmt(Long.parseLong(trdAmt))
//                                                .rsltCnfCnt(0)
//                                                .rmk(rmtOutRsltCd)
//                                                .rfdAcntBankCd(macBankCd)
//                                                .rfdAcntNoEnc(EncMacntAccNo)
//                                                .rfdAcntNoMsk(mskPayAccNo)
//                                                .rmtDivCd(RmtDivCd.DEPOSITE.getRmtDivCd())
//                                                .build();
//                                        rfdRsltCnfInsertService.insertRfdRsltCnf(rfdRsltCnfInsertDto);
//                                    }
                                } else {

                                    //tgtDpmnNoti 직전에 SET 한 데이터를 참조하지만 null 가능성을 고려해 예외처리 2025-06-19
                                    if(tgtDpmnNoti != null){
                                        String upRmk = tgtDpmnNoti.getRmk() + chrgTrdNo + ";" + rmtOutRsltCd + ";";
                                        tgtDpmnNoti.dpmnNotiRmkSave(upRmk);
                                    }

                                    try {
                                        dpmnRcvService.saveDpAmt(mid, Long.parseLong(trdAmt), globalId);
                                    } catch (Exception e) {
                                        //예치금 SC계좌 -> 우리계좌 입금완료된 상태 TB_MPS_DPMN_RCV 저장만 에러
                                        String message = String.format("선불예치 입금금액 저장 에러 [globalId: " + globalId + ", ACNT_SUMRY: " + trdSumry + ", TrdAmt: " + trdAmt + "]");
                                        log.error(message);
                                        MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    respCd = "0001";
                    String message = String.format("명세통지 저장 에러 [globalId: " + globalId + ", ACNT_SUMRY: " + trdSumry + "]");
                    log.error(message);
                    MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
                    e.printStackTrace();
                }
            }else{
                log.info("기처리거래 globalId: [{}]", globalId);
            }
        }else{
            log.info("DPMN_RCV 저장 제외 거래 globalId: [{}], 은행코드: [{}]", globalId, bankCd);
        }
        return respCd;
    }

}
