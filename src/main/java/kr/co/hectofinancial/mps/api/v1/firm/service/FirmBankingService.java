package kr.co.hectofinancial.mps.api.v1.firm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.RelayResDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.service.MpsNotiService;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.firm.dto.MoneyWithdrawRequestDto;
import kr.co.hectofinancial.mps.api.v1.firm.dto.RemittanceApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.firm.dto.RemittanceResultDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static kr.co.hectofinancial.mps.global.util.CipherUtil.encrypt;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirmBankingService {

    @Value("${rmt.url}")
    private String rmtUrl;
    @Value("${rmt.connectTimeout}")
    private int connectTimeout;
    @Value("${rmt.readTimeout}")
    private int readTimeout;
    @Value("${rmt.hdInfo}")
    private String hdInfo;
    private final CommonService commonService;
    private final MpsNotiService mpsNotiService;
    protected final ObjectMapper om;

    /* 송금 API */
    @Transactional
    public RemittanceResultDto remittanceApproval(RemittanceApprovalRequestDto remittanceApprovalRequestDto) throws Exception {

        log.info("송금_request: [{}]", remittanceApprovalRequestDto);
        String encCustNo = null;
        String encCustSumry;
        String encAccNo;
        String encMacntSumry;
        String mchtId = remittanceApprovalRequestDto.getMchtId();

        MarketAddInfoDto tgtMarketAddInfo = commonService.getMarketAddInfoByMId(mchtId);

        /* 암호화 */
        try {
            encAccNo = encrypt(remittanceApprovalRequestDto.custAcntNo, tgtMarketAddInfo.getEncKey());
            log.info("계좌번호 암호화: [{}]", encAccNo);
            if(!CommonUtil.nullTrim(remittanceApprovalRequestDto.getMchtCustId()).equals("")){
                encCustNo = encrypt(remittanceApprovalRequestDto.mchtCustId, tgtMarketAddInfo.getEncKey());
                log.info("선불고객번호 암호화: [{}]", encCustNo);
            }
            encCustSumry = encrypt(remittanceApprovalRequestDto.custAcntSumry, tgtMarketAddInfo.getEncKey());
            log.info("인자명 암호화: [{}]", encCustSumry);
            encMacntSumry = encrypt(remittanceApprovalRequestDto.getMacntSumry(), tgtMarketAddInfo.getEncKey());
        } catch (Exception e) {
            throw new RequestValidationException(ErrorCode.ENCRYPT_ERROR);
        }

        /* make pktHash */
        StringBuilder builder = new StringBuilder().append(mchtId + remittanceApprovalRequestDto.getMchtTrdNo() +
                remittanceApprovalRequestDto.getTrdDt() + remittanceApprovalRequestDto.getTrdTm() + remittanceApprovalRequestDto.getBankCd() + remittanceApprovalRequestDto.getCustAcntNo() + remittanceApprovalRequestDto.getTrdAmt() + tgtMarketAddInfo.getPktHashKey());
        String pktHash = CipherSha256Util.digestSHA256(builder.toString());

        MoneyWithdrawRequestDto moneyWithdrawRequestDto = new MoneyWithdrawRequestDto();
        moneyWithdrawRequestDto.setHdInfo(hdInfo);
        moneyWithdrawRequestDto.setMchtId(mchtId);
        moneyWithdrawRequestDto.setMchtTrdNo(remittanceApprovalRequestDto.getMchtTrdNo());
        moneyWithdrawRequestDto.setMchtCustId(encCustNo);
        moneyWithdrawRequestDto.setTrdDt(remittanceApprovalRequestDto.getTrdDt());
        moneyWithdrawRequestDto.setTrdTm(remittanceApprovalRequestDto.getTrdTm());
        moneyWithdrawRequestDto.setBankCd(remittanceApprovalRequestDto.bankCd);
        moneyWithdrawRequestDto.setCustAcntNo(encAccNo);
        moneyWithdrawRequestDto.setCustAcntSumry(encCustSumry);
        moneyWithdrawRequestDto.setTrdAmt(String.valueOf(remittanceApprovalRequestDto.trdAmt));
        moneyWithdrawRequestDto.setPktHash(pktHash);
        moneyWithdrawRequestDto.setMacntSumry(encMacntSumry);
        String apiUrl = rmtUrl + "/v1/api/pay/rmt";

        log.info("송금 API 요청 :[{}]", moneyWithdrawRequestDto);
        try {
            RelayResDto relayResDto = mpsNotiService.handleSendNoti(apiUrl, moneyWithdrawRequestDto);
            String result = String.valueOf(relayResDto.getBody());
            log.info(">>>>> 송금 API 응답 RESULT : [{}]", result);

            String json = om.writeValueAsString(relayResDto.getBody());
            RemittanceResultDto dto = om.readValue(json, new TypeReference<RemittanceResultDto>() {});
            return dto;
        }
        catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new RequestValidationException(ErrorCode.REMITTANCE_ERROR);
        }
    }

//
//    /* 입금거래명세 통지처리 */
//    @Transactional(rollbackFor = Exception.class)
//    public String depositeNotice(FirmDepositeNoticeRequestDto firmDepositeNoticeRequestDto) throws Exception {
//
//        log.info("명세통지 ENC_DATA : [{}]", firmDepositeNoticeRequestDto.getData());
//        String trdSumry;
//        String trdDt;
//        String trdTm;
//        String respData;
//        String encData = firmDepositeNoticeRequestDto.getData();
//        String decData;
//        String encRespData;
//        String respCd;
//        String mid = null;
//
//        encData = encData.substring(4);
//        try {
//            decData = SeedUtil.decrypt(encData, userKey, IV);
//            log.info("명세통지 DEC_DATA : [{}]", decData);
//        } catch (Exception e) {
//            throw new RequestValidationException(ErrorCode.DECRYPT_ERROR);
//        }
//
//        /* 응답 데이터 SET */
//        respData = decData.substring(0, 24) + "4100" + decData.substring(28);
//
//        /* 거래적요 검증 */
//        Object[] result;
//        result = CommonUtil.cutString(decData.substring(179, 191), 12, 179);
//        trdSumry = (String) result[0];
//        log.info("거래적요: [{}]", trdSumry);
//        FcrcSumry tgtFcrcSumry = fcrcSumryRepository.findByAcntSumryAndVldStDateBetween(CommonUtil.nullTrim(trdSumry));
//        if (tgtFcrcSumry != null) {
//            mid = tgtFcrcSumry.getMid();
//        }
//
//        int nextStartIndex = (int) result[1];
//        trdDt = decData.substring(nextStartIndex + 10, nextStartIndex + 18);
//        trdTm = decData.substring(nextStartIndex + 18, nextStartIndex + 24);
//        log.info("거래일자: [{}], 거래시간: [{}]", trdDt, trdTm);
//
//        String dpmnNotiResult = dpmnNotiService.saveDpmnNoti(decData, mid, trdDt, trdTm, trdSumry);
//        respCd = dpmnNotiResult;
//
//        respData = respData.substring(0, 52) + respCd + respData.substring(56);
//        try {
//            encRespData = SeedUtil.encrypt(respData, userKey, IV);
//        } catch (Exception e) {
//            throw new RequestValidationException(ErrorCode.ENCRYPT_ERROR);
//        }
//        String respHeader = "0" + encRespData.length();
//        log.info("명세통지 RESP DEC_DATA: [{}]", respData);
//        log.info("명세통지 RESP ENC_DATA: [{}]", respHeader + encRespData);
//        return respHeader + encRespData;
//    }

}
