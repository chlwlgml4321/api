package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hectofinancial.mps.api.v1.card.domain.BpcCust;
import kr.co.hectofinancial.mps.api.v1.card.repository.CardRepository;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain.NotiInfo;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.NotiCardDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.NotiCardRequsetDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.RelayResDto;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.global.constant.CnclTypeCd;
import kr.co.hectofinancial.mps.global.constant.MpsNotiTypeCd;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CardNotiService {

    @Value("${mps.card.use-stlmid}")
    private String useStlMid;
    private final MpsNotiService mpsNotiService;
    private final TradeRepository tradeRepository;
    private final CardRepository cardRepository;
    protected final ObjectMapper om;

    @Transactional(rollbackFor = Exception.class)
    public Trade getTradeInfo(String trdNo, String trdDt){
        Trade tgtTrade = tradeRepository.findByTrdNoAndTrdDt(trdNo, trdDt);
        if(tgtTrade == null) {
            log.error("카드사용거래 조회 오류!! TRD_NO: [{}], TRD_DT: [{}]", trdNo, trdDt);
            return null;
        }
        return tgtTrade;
    }

    /**
     * BPO 카드사용 알림
     */
    @Async("async") //todo 추후 타임아웃 발생시 비동기로 전환 2025-11-11, 비동기로 전환 2025-12-15
    public void sendCardApprovalInfo(NotiCardRequsetDto notiCardRequsetDto) {

        DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
        log.info("암호화된 노티 데이터 [{}]", databaseAESCryptoUtil.convertToDatabaseColumn(notiCardRequsetDto.toString()));

        log.info(">>>>> (BPO카드승인 노티 START) TRD_NO: [{}]", notiCardRequsetDto.getTrdNo());

        Trade tgtTrade = getTradeInfo(notiCardRequsetDto.getTrdNo(), notiCardRequsetDto.getTrdDt());
        if(tgtTrade == null) {
            String message = "(MPS_API) 카드승인 발송 오류 카드사용거래조회 오류! TRD_NO: " + notiCardRequsetDto.getTrdNo();
            MonitAgent.sendMonitAgent(ErrorCode.MPS_NOTI_FAIL.getErrorCode(), message);
            log.error(message);
            return;
        }

        NotiInfo tgtNotiInfo = mpsNotiService.getNotiUrl(tgtTrade.getMid(), MpsNotiTypeCd.CARD_APPROVAL.getNotiTypeCd());
        if (tgtNotiInfo == null){
            log.info(">>>>> (BPO카드승인) 노티 받지 않는 가맹점 : [{}], 선불회원번호: [{}]", tgtTrade.getMid(), tgtTrade.getMpsCustNo());
            return;
        }

        log.info("(BPO카드승인) 노티 URL: " + tgtNotiInfo.getNotiUrl());

        NotiCardDto notiCardDto = new NotiCardDto();
        String body;

        try {
            String mpsCustNo = tgtTrade.getMpsCustNo();

            if(!tgtTrade.getStorCd().equals("BCC")){
                log.error("BPO카드 사용 거래아님! TRD_NO: [{}], 선불회원번호: [{}], STOR_CD: [{}]", tgtTrade.getTrdNo(), mpsCustNo, tgtTrade.getStorCd());
                return;
            }

            if (!tgtTrade.getStlMId().equals(useStlMid)) {
                log.error("BPO카드 사용 거래아님! TRD_NO: [{}], 선불회원번호: [{}], STL_M_ID: [{}]", tgtTrade.getTrdNo(), mpsCustNo, tgtTrade.getStlMId());
                return;
            }

            BpcCust tgtBpcCust = cardRepository.findByCardMngNo(tgtTrade.getCardMngNo());

            String encKey = notiCardRequsetDto.getEncKey();
            String mCustId = tgtTrade.getMCustId();
            String tradeDate = tgtTrade.getTrdDt() + tgtTrade.getTrdTm();
            String mnyAmt = String.valueOf(tgtTrade.getMnyAmt());
            String pntAmt = String.valueOf(tgtTrade.getPntAmt());
            String waitMnyAmt = String.valueOf(tgtTrade.getWaitMnyAmt());
            String mnyBlc = String.valueOf(tgtTrade.getMnyBlc());
            String pntBlc = String.valueOf(tgtTrade.getPntBlc());
            String waitMnyBlc = String.valueOf(tgtTrade.getWaitMnyBlc());
            String joinNo = tgtTrade.getMResrvField2();
            String joinNm = tgtTrade.getMResrvField1();
            String prdtCd = tgtBpcCust.getPtnPrdtNo();

            String tradeType = "";
            tradeType  = CnclTypeCd.getTradeType(tgtTrade.getCnclTypeCd());
            if(tradeType == null){
                if (tgtTrade.getTrdDivCd().equals(TrdDivCd.COMMON_USE.getTrdDivCd())) {
                    tradeType = "APPROVAL";
                } else if (tgtTrade.getTrdDivCd().equals(TrdDivCd.USE_COMMON_CANCEL.getTrdDivCd())) {
                    tradeType = "APPROVAL_CANCEL";
                } else {
                    log.error("매입취소.무승인매입.무승인매입취소 거래가 아닌 다른 거래 => CNCL_TYPE_CD: [{}], TRD_NO: [{}], 선불회원번호: [{}]", tgtTrade.getCnclTypeCd(), tgtTrade.getTrdNo(), mpsCustNo);
                    return;
                }
            }
            log.info("카드승인노티 거래유형:[{}]", tradeType);

            notiCardDto.setCustNo(CipherUtil.encrypt(mpsCustNo, encKey));
            notiCardDto.setMCustId(CipherUtil.encrypt(mCustId, encKey));
            notiCardDto.setTrdNo(CipherUtil.encrypt(tgtTrade.getTrdNo(), encKey));
            notiCardDto.setTradeDate(CipherUtil.encrypt(tradeDate, encKey));
            notiCardDto.setMnyAmt(CipherUtil.encrypt(mnyAmt, encKey));
            notiCardDto.setPntAmt(CipherUtil.encrypt(pntAmt, encKey));
            notiCardDto.setWaitMnyAmt(CipherUtil.encrypt(waitMnyAmt, encKey));
            notiCardDto.setMnyBlc(CipherUtil.encrypt(mnyBlc, encKey));
            notiCardDto.setPntBlc(CipherUtil.encrypt(pntBlc, encKey));
            notiCardDto.setWaitMnyBlc(CipherUtil.encrypt(waitMnyBlc, encKey));
            notiCardDto.setTradeType(CipherUtil.encrypt(tradeType, encKey));
            notiCardDto.setJoinNo(CipherUtil.encrypt(joinNo, encKey));
            notiCardDto.setJoinNm(CipherUtil.encrypt(joinNm, encKey));
            notiCardDto.setPrdtNo(CipherUtil.encrypt(prdtCd, encKey));
            if(!CommonUtil.nullTrim(notiCardRequsetDto.getOrgTrdDt()).equals("") && !CommonUtil.nullTrim(notiCardRequsetDto.getOrgTrdNo()).equals("")){
                log.info("원거래승인번호: [{}], 원거래승인일자: [{}]", notiCardRequsetDto.getOrgTrdNo(), notiCardRequsetDto.getOrgTrdDt());
                notiCardDto.setOrgTrdDt(CipherUtil.encrypt(notiCardRequsetDto.getOrgTrdDt(), encKey));
                notiCardDto.setOrgTrdNo(CipherUtil.encrypt(notiCardRequsetDto.getOrgTrdNo(), encKey));
            }

            notiCardDto.setPktHash(CipherSha256Util.digestSHA256(mpsCustNo + mCustId + notiCardRequsetDto.getTrdNo() + tradeType + mnyAmt + pntAmt + notiCardRequsetDto.getPktHashKey()));

            log.info("notiCardDto :[{}]", notiCardDto);
            om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            body = om.writeValueAsString(notiCardDto);
        }
        catch(Exception e) {
            e.printStackTrace();
            String message = "(MPS_API) 카드승인 발송데이터 설정 중 오류 발생! M_ID: " + tgtTrade.getMid() + ", TRD_NO: " + tgtTrade.getTrdNo() + ", 선불회원번호: " + tgtTrade.getMpsCustNo();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(MPS_API) 카드승인 발송데이터 설정 중 오류 발생!")
                    .append("\n")
                    .append("======================")
                    .append("\n")
                    .append("선불회원번호: "+tgtTrade.getMpsCustNo())
                    .append("\n")
                    .append("M_ID: "+tgtTrade.getMid())
                    .append("\n")
                    .append("TRD_NO : "+tgtTrade.getTrdNo());
            MonitAgent.sendMonitAgent(ErrorCode.MPS_NOTI_FAIL.getErrorCode(), stringBuilder.toString());
            log.error(message);
            return;
        }

        log.info("Send update card approval noti. " + body);

        try {
            RelayResDto relayResDto = mpsNotiService.handleSendNoti(tgtNotiInfo.getNotiUrl(), notiCardDto);
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
            String message = "(MPS_API) 카드승인 노티 발송 실패! M_ID: " + tgtTrade.getMid() + ", TRD_NO: " + tgtTrade.getTrdNo() + ", 선불회원번호: " + tgtTrade.getMpsCustNo();
            log.error(message);
            mpsNotiService.handleReSendNoti(tgtTrade.getMpsCustNo(), tgtTrade.getMid(), body, tgtNotiInfo); //1차노티 실패, 리노티 등록
        }
    }

}