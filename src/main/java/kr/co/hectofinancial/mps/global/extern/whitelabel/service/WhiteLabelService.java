package kr.co.hectofinancial.mps.global.extern.whitelabel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.PgPayRequestDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.RelayResDto;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.error.exception.RelayServerException;
import kr.co.hectofinancial.mps.global.error.exception.WhiteLabelException;
import kr.co.hectofinancial.mps.global.extern.whitelabel.constant.WhiteLabelKindCd;
import kr.co.hectofinancial.mps.global.extern.whitelabel.dto.WhiteLabelAuthRequestDto;
import kr.co.hectofinancial.mps.global.extern.whitelabel.dto.WhiteLabelAuthResponseDto;
import kr.co.hectofinancial.mps.global.extern.whitelabel.dto.WhiteLabelPayRequestDto;
import kr.co.hectofinancial.mps.global.extern.whitelabel.dto.WhiteLabelPayResponseDto;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.RelayConnectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhiteLabelService {

    @Value("${whitelabel.url}")
    private String WHITELABEL_URL;
    @Value("${spring.profiles.active}")
    private String profiles;
    private static final String SUCCESS_STATUS_CODE = "0021"; //화이트라벨 성공 기본값
    private static final String SUCCESS_RESULT_CODE = "0000"; //화이트라벨 성공 기본값
    private final RelayConnectionUtil relayConnectionUtil;
    private final ObjectMapper om;

    /**
     * 화이트라벨 인증 -> 결제 승인
     *
     * @param pgPayRequestDto
     * @return PG거래번호
     * @throws WhiteLabelException
     */
    public WhiteLabelPayResponseDto.Params processPayment(PgPayRequestDto pgPayRequestDto) throws WhiteLabelException {

        String encKey = pgPayRequestDto.getEncKey();
        String hashKey = pgPayRequestDto.getPktHashKey();
        String authTrdNo = "";

        /* auth 시작 */
        if (pgPayRequestDto.getChrgMeanCd() == TrdChrgMeanCd.RP) { //계좌
            WhiteLabelAuthResponseDto authResponseDto = accountAuth(pgPayRequestDto, encKey, hashKey);
            authTrdNo = authResponseDto.getData().getAuthTrdNo();
        } else if (pgPayRequestDto.getChrgMeanCd() == TrdChrgMeanCd.CREDITCARD_APPROVAL) { //카드

        }

        /* pay 시작 */
        WhiteLabelPayResponseDto payResponseDto = payConfirm(pgPayRequestDto, authTrdNo, encKey, hashKey);
        return payResponseDto.getParams();
//        return payResponseDto.getParams().getTrdNo();


    }

    /**
     * 화이트라벨 계좌출금 인증
     *
     * @param requestDto
     * @param encKey
     * @param hashKey
     * @return
     * @throws WhiteLabelException
     */
    private WhiteLabelAuthResponseDto accountAuth(PgPayRequestDto requestDto, String encKey, String hashKey) throws WhiteLabelException {

        //계좌 출금 인증 url
        WhiteLabelKindCd kindCd = WhiteLabelKindCd.AUTH_WITHDRAWAL;
        String kindNm = kindCd.getEndPointEng();

        String authUrl = (WHITELABEL_URL + kindCd.getEndPoint());
        String mpsCustNo = requestDto.getMpsCustNo();
        String mid = requestDto.getMid();
        String mchtCustId = requestDto.getMchtCustId();
        String mTrdNo = getWhiteLabelMTrdNo(requestDto.getMTrdNo(), requestDto.getAutoChargeType().getValue(), requestDto.getTryCnt());  //2회차 이상부터는 mTrdNo_회차 로 변환
        String reqDt = requestDto.getReqDt();
        String reqTm = requestDto.getReqTm();
        String custAcntKey = requestDto.getCustAcntKey();
        String bankCd = requestDto.getBankCd();
        long reqAmt = requestDto.getReqAmt();
        String sumry = requestDto.getSumry();

        log.info("[WHITELABEL][{}][START] custNo={} mchtCustId={} mTrdNo={}", kindNm, mpsCustNo, mchtCustId, mTrdNo);

        //해시문자열 조합
        String prePktHash = reqDt + reqTm + mid + mTrdNo + mchtCustId + hashKey;

        WhiteLabelAuthRequestDto.Params params = WhiteLabelAuthRequestDto.Params.builder()
                .mchtId(mid) //상점아이디
                .mchtTrdNo(mTrdNo) //상점거래번호
                .trdDt(reqDt) //거래일자
                .trdTm(reqTm) //거래시각
                .build();

        String pktHash = null;
        String trdAmtEnc = null;

        try {
            pktHash = CipherSha256Util.digestSHA256(prePktHash);
            trdAmtEnc = CipherUtil.encrypt(String.valueOf(reqAmt), encKey);
        } catch (NoSuchAlgorithmException e) {
            //pktHash 생성 실패
            log.error("[WHITELABEL][{}][FAIL: making pktHash out of {}] custNo={} mchtCustId={} mTrdNo={}", kindNm, prePktHash, mpsCustNo, mchtCustId, mTrdNo);
            throw WhiteLabelException.preValidation(kindCd, "request pktHash 생성 실패", e);
        } catch (Exception e) {
            //reqAmt 암호화 실패
            log.error("[WHITELABEL][{}][FAIL: to encrypt reqAmt={}] custNo={} mchtCustId={} mTrdNo={}", kindNm, reqAmt, mpsCustNo, mchtCustId, mTrdNo);
            throw WhiteLabelException.preValidation(kindCd, "request 내 trdAmt 암호화 실패", e);
        }

        WhiteLabelAuthRequestDto.Datas datas = WhiteLabelAuthRequestDto.Datas.builder()
                .pktHash(pktHash)
                .mchtCustId(mchtCustId) //고객아이디
                .bankCd(bankCd) //은행코드
                .custAcntKey(custAcntKey) //헥토계좌번호키
                .trdAmt(trdAmtEnc) //거래금액
                .sumry(sumry) //고객계좌 출금적요
                .build();

        WhiteLabelAuthRequestDto authRequestDto = WhiteLabelAuthRequestDto.builder()
                .params(params)
                .data(datas)
                .build();

        try {

            RelayResDto relayResDto = relayConnectionUtil.sendRequest(authUrl, authRequestDto);
            WhiteLabelAuthResponseDto authResponseDto = om.convertValue(relayResDto.getBody(), WhiteLabelAuthResponseDto.class);

            //응답값 확인
            String outStatCd = authResponseDto.getParams().getOutStatCd();
            String outRsltCd = authResponseDto.getParams().getOutRsltCd();
            String outRsltMsg = authResponseDto.getParams().getOutRsltMsg();

            if (!SUCCESS_STATUS_CODE.equals(outStatCd) || !SUCCESS_RESULT_CODE.equals(outRsltCd)) {
                //0021 이나 0000 아니면 실패
                log.error("[WHITELABEL][{}][FAIL: outStatCd={} outRsltCd={} outRsltMsg={}] custNo={} mchtCustId={} mTrdNo={}", kindNm, outStatCd, outRsltCd, outRsltMsg, mpsCustNo, mchtCustId, mTrdNo);
                throw WhiteLabelException.apiResponseFail(kindCd, outStatCd, outRsltCd, outRsltMsg);
            }

            String authTrdNo = authResponseDto.getData().getAuthTrdNo();
            if (StringUtils.isBlank(authTrdNo)) {
                //authTrdNo 없음
                log.error("[WHITELABEL][{}][FAIL: authTrdNo is empty] custNo={} mchtCustId={} mTrdNo={}", kindNm, mpsCustNo, mchtCustId, mTrdNo);
                throw WhiteLabelException.postValidation(kindCd, "authTrdNo 없음!! ");
            }

            //응답값 내 pktHash 확인
            String postPktHash = authResponseDto.getParams().getOutStatCd() + authResponseDto.getParams().getTrdDt() + authResponseDto.getParams().getTrdTm() + mid + mTrdNo + mchtCustId + hashKey;
            checkResponsePktHash(kindCd, authResponseDto.getData().getPktHash(), postPktHash, mpsCustNo, mchtCustId, mTrdNo);

            //WhiteLabelAuthResponseDto 객체 return
            log.info("[WHITELABEL][{}][SUCCESS] custNo={} mchtCustId={} mTrdNo={} authTrdNo={}", kindNm, mpsCustNo, mchtCustId, mTrdNo, authResponseDto.getData().getAuthTrdNo());
            return authResponseDto;
        } catch (WhiteLabelException e) {
            //이미 로그찍고 감싸진 whitelabel exception 그대로 throw
            throw e;
        } catch (RelayServerException e) {
            //릴레이 연동 실패
            log.error("[WHITELABEL][{}][FAIL: relay server error] custNo={} mchtCustId={} mTrdNo={}", kindNm, mpsCustNo, mchtCustId, mTrdNo);
            throw WhiteLabelException.apiConnectionFail(kindCd, e.getMessage(), e);
        } catch (Exception e) {
            //기타 에러
            log.error("[WHITELABEL][{}][FAIL] custNo={} mchtCustId={} mTrdNo={}", kindNm, mpsCustNo, mchtCustId, mTrdNo, e);
            throw WhiteLabelException.otherError(kindCd, e.getMessage(), e);
        }
    }


    /**
     * 화이트라벨 결제 승인
     *
     * @param requestDto
     * @param authTrdNo
     * @param encKey
     * @param hashKey
     * @return
     * @throws WhiteLabelException
     */
    private WhiteLabelPayResponseDto payConfirm(PgPayRequestDto requestDto, String authTrdNo, String encKey, String hashKey) throws WhiteLabelException {

        //결제 승인 인증 url
        WhiteLabelKindCd kindCd = WhiteLabelKindCd.PAY_CONFIRM;
        String kindNm = kindCd.getEndPointEng();

        String payUrl = (WHITELABEL_URL + kindCd.getEndPoint());
        String mpsCustNo = requestDto.getMpsCustNo();
        String mid = requestDto.getMid();
        String mchtCustId = requestDto.getMchtCustId();
        String mTrdNo = getWhiteLabelMTrdNo(requestDto.getMTrdNo(), requestDto.getAutoChargeType().getValue(), requestDto.getTryCnt());
        String reqDt = requestDto.getReqDt();
        String reqTm = requestDto.getReqTm();
        long reqAmt = requestDto.getReqAmt();

        log.info("[WHITELABEL][{}][START] custNo={} mchtCustId={} mTrdNo={}", kindNm, mpsCustNo, mchtCustId, mTrdNo);

        //해시문자열 조합
        String prePktHash = reqDt + reqTm + mid + mTrdNo + reqAmt + hashKey;

        WhiteLabelPayRequestDto.Params params = WhiteLabelPayRequestDto.Params.builder()
                .mchtId(mid) //상점아이디
                .mchtTrdNo(mTrdNo) //상점거래번호
                .trdDt(reqDt) //거래일자
                .trdTm(reqTm) //거래시각
                .build();

        String pktHash = null;
        String trdAmtEnc = null;

        try {
            pktHash = CipherSha256Util.digestSHA256(prePktHash);
            trdAmtEnc = CipherUtil.encrypt(String.valueOf(reqAmt), encKey);
        } catch (NoSuchAlgorithmException e) {
            //pktHash 생성 실패
            log.error("[WHITELABEL][{}][FAIL: making pktHash out of {}] custNo={} mchtCustId={} mTrdNo={}", kindNm, prePktHash, mpsCustNo, mchtCustId, mTrdNo);
            throw WhiteLabelException.preValidation(kindCd, "request pktHash 생성 실패", e);
        } catch (Exception e) {
            //암호화 실패 (reqAmt)
            log.error("[WHITELABEL][{}][FAIL: to encrypt reqAmt={}] custNo={} mchtCustId={} mTrdNo={}", kindNm, reqAmt, mpsCustNo, mchtCustId, mTrdNo);
            throw WhiteLabelException.preValidation(kindCd, "request 내 trdAmt 암호화 실패", e);
        }

        WhiteLabelPayRequestDto.Datas datas = WhiteLabelPayRequestDto.Datas.builder()
                .pktHash(pktHash) //해시키
                .mchtCustId(mchtCustId) //고객아이디
                .authTrdNo(authTrdNo) //거래인증번호
                .trdAmt(trdAmtEnc) //거래금액
                .build();

        WhiteLabelPayRequestDto payRequestDto = WhiteLabelPayRequestDto.builder()
                .params(params)
                .data(datas)
                .build();

        try {

            RelayResDto relayResDto = relayConnectionUtil.sendRequest(payUrl, payRequestDto);
            WhiteLabelPayResponseDto payResponseDto = om.convertValue(relayResDto.getBody(), WhiteLabelPayResponseDto.class);

            //응답값 확인
            String outStatCd = payResponseDto.getParams().getOutStatCd();
            String outRsltCd = payResponseDto.getParams().getOutRsltCd();
            String outRsltMsg = payResponseDto.getParams().getOutRsltMsg();

            if (!SUCCESS_STATUS_CODE.equals(outStatCd) || !SUCCESS_RESULT_CODE.equals(outRsltCd)) {
                //0021 이나 0000 아니면 실패
                log.error("[WHITELABEL][{}][FAIL: outStatCd={} outRsltCd={} outRsltMsg={}] custNo={} mchtCustId={} mTrdNo={}", kindNm, outStatCd, outRsltCd, outRsltMsg, mpsCustNo, mchtCustId, mTrdNo);
                throw WhiteLabelException.apiResponseFail(kindCd, outStatCd, outRsltCd, outRsltMsg);
            }
            String trdNo = payResponseDto.getParams().getTrdNo();
            if (StringUtils.isBlank(trdNo)) {
                //trdNo 없음
                log.error("[WHITELABEL][{}][FAIL: trdNo is empty] custNo={} mchtCustId={} mTrdNo={}", kindNm, mpsCustNo, mchtCustId, mTrdNo);
                throw WhiteLabelException.postValidation(kindCd, "trdNo 없음!! ");
            }

            //응답값 내 pktHash 확인
            String postPktHash = payResponseDto.getParams().getOutStatCd() + payResponseDto.getParams().getTrdDt() + payResponseDto.getParams().getTrdTm() + mid + authTrdNo + mchtCustId + hashKey;
            checkResponsePktHash(WhiteLabelKindCd.AUTH_WITHDRAWAL, payResponseDto.getData().getPktHash(), postPktHash, mpsCustNo, mchtCustId, mTrdNo);

            //WhiteLabelPayResponseDto 객체 return
            log.info("[WHITELABEL][{}][SUCCESS] custNo={} mchtCustId={} mTrdNo={} trdNo={}", kindNm, mpsCustNo, mchtCustId, mTrdNo, payResponseDto.getParams().getTrdNo());
            return payResponseDto;
        } catch (WhiteLabelException e) {
            //이미 로그찍고 감싸진 whitelabel exception 그대로 throw
            throw e;
        } catch (RelayServerException e) {
            //릴레이 연동 실패
            log.error("[WHITELABEL][{}][FAIL: relay server error] custNo={} mchtCustId={} mTrdNo={}", kindNm, mpsCustNo, mchtCustId, mTrdNo);
            throw WhiteLabelException.apiConnectionFail(kindCd, e.getMessage(), e);
        } catch (Exception e) {
            //기타 에러
            log.error("[WHITELABEL][{}][FAIL] custNo={} mchtCustId={} mTrdNo={}", kindNm, mpsCustNo, mchtCustId, mTrdNo, e);
            throw WhiteLabelException.otherError(kindCd, e.getMessage(), e);
        }
    }

    /**
     * 화이트라벨용 M_TRD_NO 중복 방지용
     *
     * @param mTrdNo
     * @param tryCnt
     * @return
     */
    public static String getWhiteLabelMTrdNo(String mTrdNo, String autoChrgType, int tryCnt) {
        mTrdNo += ("_" + autoChrgType);
        return tryCnt > 1 ? (mTrdNo + "_" + tryCnt) : mTrdNo;
    }


    /**
     * 화이트라벨 연동 후 pktHash 확인
     *
     * @param kind
     * @param pktHashInResponse
     * @param pktHashStr
     * @param mpsCustNo
     * @param mchtCustId
     * @param mTrdNo
     * @throws WhiteLabelException
     */
    private void checkResponsePktHash(WhiteLabelKindCd kind, String pktHashInResponse, String pktHashStr, String mpsCustNo, String mchtCustId, String mTrdNo) throws WhiteLabelException {
        String kindNm = kind.getEndPointEng();
        try {
            //응답값 가지고 만든 pktHash 검증
            String madePktHash = CipherSha256Util.digestSHA256(pktHashStr);
            if (!pktHashInResponse.equals(madePktHash)) {
                log.error("[WHITELABEL][{}][FAIL: response pktHash validation fail! postPktHash={} => {} != {}] custNo={} mchtCustId={} mTrdNo={}", kindNm, pktHashStr, madePktHash, pktHashInResponse, mpsCustNo, mchtCustId, mTrdNo);
                throw WhiteLabelException.postValidation(kind, "응답값 pktHash 불일치");
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("[WHITELABEL][{}][FAIL: making pktHash out of {}] custNo={} mchtCustId={} mTrdNo={}", kindNm, pktHashStr, mpsCustNo, mchtCustId, mTrdNo);
            throw WhiteLabelException.postValidation(kind, "response pktHash 생성 실패", e);
        }
    }
}
