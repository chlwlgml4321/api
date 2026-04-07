package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain.NotiInfo;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.NotiAutoChargeDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.NotiAutoChargeRequsetDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.RelayResDto;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.global.constant.MpsNotiTypeCd;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AutoChargeNotiService {

    private final MpsNotiService mpsNotiService;
    private final TradeRepository tradeRepository;
    protected final ObjectMapper om;

    @Transactional(rollbackFor = Exception.class)
    public Trade getTradeInfo(String trdNo, String trdDt){
        Trade tgtTrade = tradeRepository.findByTrdNoAndTrdDt(trdNo, trdDt);
        if(tgtTrade == null) {
            log.error("자동충전거래 조회 오류!! TRD_NO: [{}], TRD_DT: [{}]", trdNo, trdDt);
            return null;
        }
        return tgtTrade;
    }

    /**
     * 자동충전 거래 알림
     */
    @Async("async")
    public void sendAutoChargeInfo(NotiAutoChargeRequsetDto notiAutoChargeRequsetDto) {

        log.info(">>>>> (자동충전 노티 START) M_ID: [{}], MPS_CUST_NO: [{}], RSLT_CD: [{}], RSLT_MSG: [{}]", notiAutoChargeRequsetDto.getMid(), notiAutoChargeRequsetDto.getCustNo(), notiAutoChargeRequsetDto.getRsltCd(), notiAutoChargeRequsetDto.getRsltMsg());

        DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
        log.info("암호화된 노티 데이터 [{}]", databaseAESCryptoUtil.convertToDatabaseColumn(notiAutoChargeRequsetDto.toString()));

        NotiAutoChargeDto notiAutoChargeDto = new NotiAutoChargeDto();
        String body;

        String trdNo = "";
        String trdDt = ""; //trdNo의 거래일자
        String mnyAmt = "";
        String chrgTrdNo = "";
        String chrgTradDate = ""; //화이트라벨 api 응답값 내 시간 (고객통장출금시각)

        String rsltCd = notiAutoChargeRequsetDto.getRsltCd();
        String rsltMsg = notiAutoChargeRequsetDto.getRsltMsg();
        String mid = notiAutoChargeRequsetDto.getMid();
        String mpsCustNo = notiAutoChargeRequsetDto.getCustNo();
        String mCustId = notiAutoChargeRequsetDto.getMCustId();
        String chargeType = notiAutoChargeRequsetDto.getChargeType();
        String mnyBlc = notiAutoChargeRequsetDto.getMnyBlc();
        String bankCd = notiAutoChargeRequsetDto.getBankCd();
        String bankInfo = notiAutoChargeRequsetDto.getBankInfo();
        String tradeDate = notiAutoChargeRequsetDto.getTradeDate(); //거래테이블에 trdDt, trdTm (거래구분코드 MP)
        String encKey = notiAutoChargeRequsetDto.getEncKey();
        String pktHash = notiAutoChargeRequsetDto.getPktHash();

        NotiInfo tgtNotiInfo = mpsNotiService.getNotiUrl(mid, MpsNotiTypeCd.AUTO_CHARGE.getNotiTypeCd());
        if (tgtNotiInfo == null) {
            log.info(">>>>> (자동충전) 노티 받지 않는 가맹점 : [{}], 선불회원번호: [{}], 자동충전유형: [{}]", mid, mpsCustNo, chargeType);
            return;
        }

        log.info("(자동충전) 노티 URL: " + tgtNotiInfo.getNotiUrl());

        try {

            if (notiAutoChargeRequsetDto.getRsltCd().equals("0000")) {

                trdNo = notiAutoChargeRequsetDto.getTrdNo();
                trdDt = notiAutoChargeRequsetDto.getTrdDt();
                chrgTrdNo = notiAutoChargeRequsetDto.getChrgTrdNo();
                chrgTradDate = notiAutoChargeRequsetDto.getChrgTradeDate();

                Trade tgtTrade = getTradeInfo(trdNo, trdDt);
                if (tgtTrade == null) { //TODO 추후 삭제해도 됨
                    String message = "(MPS_API) 자동충전 노티 발송 오류 자동충전거래조회 오류! TRD_NO: " + trdNo + ", 자동충전유형: " + chargeType;
                    MonitAgent.sendMonitAgent(ErrorCode.MPS_NOTI_FAIL.getErrorCode(), message);
                    log.error(message);
                    return;
                }

                tradeDate = tgtTrade.getTrdDt() + tgtTrade.getTrdTm();
                mpsCustNo = tgtTrade.getMpsCustNo();
                mCustId = tgtTrade.getMCustId();
                mnyAmt = String.valueOf(tgtTrade.getMnyAmt());
                mnyBlc = String.valueOf(tgtTrade.getMnyBlc());
                if(!tgtTrade.getTrdDivCd().equals(TrdDivCd.MONEY_PROVIDE.getTrdDivCd())){
                    log.error("자동충전 거래아님! TRD_NO: [{}], 선불회원번호: [{}], TRD_DIV_CD: [{}], 자동충전유형: [{}]", tgtTrade.getTrdNo(), mpsCustNo, tgtTrade.getTrdDivCd(), chargeType);
                    return;
                }

                if (!tgtTrade.getChrgMeanCd().equals(TrdChrgMeanCd.RP.getChrgMeanCd())) {
                    log.error("자동충전 거래아님! TRD_NO: [{}], 선불회원번호: [{}], CHRG_MEAN_CD: [{}], 자동충전유형: [{}]", tgtTrade.getTrdNo(), mpsCustNo, tgtTrade.getChrgMeanCd(), chargeType);
                    return;
                }

                notiAutoChargeDto.setTrdNo(CipherUtil.encrypt(trdNo, encKey));
                notiAutoChargeDto.setMnyAmt(CipherUtil.encrypt(mnyAmt, encKey));
                notiAutoChargeDto.setChrgTrdNo(CipherUtil.encrypt(chrgTrdNo, encKey));
                notiAutoChargeDto.setChrgTradeDate(CipherUtil.encrypt(chrgTradDate, encKey));
                notiAutoChargeDto.setPktHash(CipherSha256Util.digestSHA256(rsltCd + mpsCustNo + mCustId + trdNo + chargeType + mnyAmt + bankCd + pktHash));
            }else{
                notiAutoChargeDto.setPktHash(CipherSha256Util.digestSHA256(rsltCd + mpsCustNo + mCustId  + chargeType  +  bankCd + pktHash));
            }

            notiAutoChargeDto.setTradeDate(CipherUtil.encrypt(tradeDate, encKey));
            notiAutoChargeDto.setCustNo(CipherUtil.encrypt(mpsCustNo, encKey));
            notiAutoChargeDto.setMCustId(CipherUtil.encrypt(mCustId, encKey));
            notiAutoChargeDto.setMnyBlc(CipherUtil.encrypt(mnyBlc, encKey));
            notiAutoChargeDto.setChargeType(CipherUtil.encrypt(chargeType, encKey));
            notiAutoChargeDto.setBankCd(CipherUtil.encrypt(bankCd, encKey));
            notiAutoChargeDto.setBankInfo(CipherUtil.encrypt(bankInfo, encKey));
            notiAutoChargeDto.setRsltCd(CipherUtil.encrypt(rsltCd, encKey));
            notiAutoChargeDto.setRsltMsg(CipherUtil.encrypt(rsltMsg, encKey));
            log.info("자동충전 노티>> trdNo={} trdDt={} chrgTradeDate={} tradeDate={}", trdNo, trdDt, chrgTradDate, tradeDate);

            om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            body = om.writeValueAsString(notiAutoChargeDto);
        }

        catch(Exception e) {
            String message = "(MPS_API) 자동충전 노티 발송데이터 설정 중 오류 발생! M_ID: " + mid + ", TRD_NO: " + trdNo + ", 선불회원번호: " + mpsCustNo
                    + ", 자동충전유형: " + chargeType + ", RSLT_CD: " + rsltCd + ", RSLT_MSG: " + rsltMsg;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(MPS_API) 자동충전 노티 발송데이터 설정 중 오류 발생")
                    .append("\n")
                    .append("======================")
                    .append("\n")
                    .append("선불회원번호: "+mpsCustNo)
                    .append("\n")
                    .append("M_ID: "+mid)
                    .append("\n")
                    .append("TRD_NO : "+trdNo)
                    .append("\n")
                    .append("자동충전유형: " +chargeType)
                    .append("\n")
                    .append("RSLT_CD(자동충전 결과코드): " + rsltCd)
                    .append("\n")
                    .append("RSLT_MSG(자동충전 결과메시지): " + rsltMsg)
                    .append("\n")
                    .append("======================");
            MonitAgent.sendMonitAgent(ErrorCode.MPS_NOTI_FAIL.getErrorCode(), stringBuilder.toString());
            log.error(message, e);
            return;
        }

        log.info("Send update auto_charge noti. " + body);

        try {
            RelayResDto relayResDto = mpsNotiService.handleSendNoti(tgtNotiInfo.getNotiUrl(), notiAutoChargeDto);
            String status = relayResDto.getStatus();
            log.info(">>>>> 가맹점 노티 응답 STATUS : [{}]", status);
            String result = String.valueOf(relayResDto.getBody());
            log.info(">>>>> 가맹점 노티 응답 RESULT : [{}]", result);

            if (!"OK".equals(result) && !"\"OK\"".equals(result)) {
                log.info("response external notificaiton body=[{}]", relayResDto.getBody());
                throw new RuntimeException("response external notificaiton body=" + relayResDto.getBody());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            String message = "(MPS_API) 자동충전 노티 발송 실패! M_ID: " + mid + ", TRD_NO: " + trdNo + ", 선불회원번호: " + mpsCustNo
                    + ", 자동충전유형: " + chargeType + ", RSLT_CD: " + rsltCd + ", RSLT_MSG: " + rsltMsg;
            log.error(message);
            mpsNotiService.handleReSendNoti(mpsCustNo, mid, body, tgtNotiInfo); //1차노티 실패, 리노티 등록
        }
    }

}