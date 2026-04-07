package kr.co.hectofinancial.mps.test.feign;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static kr.co.hectofinancial.mps.global.util.CipherSha256Util.digestSHA256;
import static kr.co.hectofinancial.mps.global.util.CipherUtil.decrypt;
import static kr.co.hectofinancial.mps.global.util.CipherUtil.encrypt;
@Slf4j
public class FeignClientUtils {
    /**
     * getEncValue 암호화된 문자열, getDecValue 복호화, makePktHash 해시문자열
     * 위의 메서드들은 모두 오버라이딩 되어있으므로
     * 첫번째 파라미터에 "prd" 를 입력하여 호출시 운영용으로 사용가능, (default 는 TB 용으로 사용)
     */
    class TB { //TB
        public static final String ENV_NAME = "test";
        public static final String MID = "M2471645";
        public static final String ENK_KEY = "SETTLEBANKISGOODSETTLEBANKISGOOD"; //TSTDB 의MID = M2471645 의 AES 암호화키
        public static final String HASH_KEY = "ST1803211422454087540";
    }
    class CARD_TB {
        public static final String ENV_NAME = "test";
        public static final String MID = "M2492185";
        public static final String ENK_KEY = "SETTLEBANKISGOODSETTLEBANKISGOOD"; //TSTDB 의MID = M2471645 의 AES 암호화키
        public static final String HASH_KEY = "ST1803211422454087540";
    }
    class PRD { //PRD
        public static final String ENV_NAME = "prd";
        public static final String MID = "M2485302";
        public static final String ENK_KEY = "561hR351U5r6B0HUCJk50p0MNbIXJ4RY";
        public static final String HASH_KEY = "ST2411011342195469789";
    }


    private static final String RSLT_CD = "rsltCd";
    private static final String RSLT_MSG = "rsltMsg";
    private static final String RSLT_OBJ = "rsltObj";

    public static String getEncVal(String profile, String str) {
        try {
            return encrypt(str, (!profile.contains("prd")) ? TB.ENK_KEY : PRD.ENK_KEY);
        } catch (Exception e) {
            return str;
        }
    }
    public static String getDecVal(String profile, String str) {
        try {
            return decrypt(str, (!profile.contains("prd")) ? TB.ENK_KEY : PRD.ENK_KEY);
        } catch (Exception e) {
            return str;
        }
    }

    public static String makePktHash(String profile, FeignClientRequestDto requestDto) {
        String pktHashStr = "";
        String trdDivCd = StringUtils.isEmpty(requestDto.getTrdDivCd()) ? requestDto.getDivCd() : requestDto.getTrdDivCd();

        String mId = (!profile.contains("prd")) ? TB.MID: PRD.MID;
        String hashKey = (!profile.contains("prd")) ? TB.HASH_KEY : PRD.HASH_KEY;

        if (StringUtils.isNotBlank(requestDto.getMid())) {
            mId = requestDto.getMid();
        }
        switch (trdDivCd){
            case "CU"://선불회원번호,상점아이디,상점거래번호,거래금액
            case "MP":
            case "PP":
            case "MW":
            case "MG":
                pktHashStr = requestDto.getCustNo() + mId + requestDto.getMTrdNo() + requestDto.getTrdAmt();
                break;
            case "UC"://선불회원번호,상점아이디,상점거래번호,원거래승인번호,원거래일자
            case "MC":
            case "PC":
                pktHashStr = requestDto.getCustNo() + mId + requestDto.getMTrdNo() + requestDto.getOrgTrdNo() + requestDto.getOrgTrdDt();
                break;
            case "WW"://선불회원번호,상점아이디,상점거래번호,대기머니잔액
                pktHashStr = requestDto.getCustNo() + mId + requestDto.getMTrdNo() + requestDto.getWaitMnyBlc();
                break;
            case "CUE"://각각사용 임시
                pktHashStr = requestDto.getCustNo() + mId + requestDto.getMTrdNo() + requestDto.getMnyAmt() + requestDto.getPntAmt();
        }
        //마지막으로 해시키
        try {
            pktHashStr = digestSHA256(pktHashStr + hashKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return pktHashStr;
    }

    public static void validatePktHash(String profile, Map<String, Object> resultObj, String trdDivCd) {

        String pktHashStr = (String) resultObj.get("pktHash");

        String custNo = (String) resultObj.get("custNo");
        String mId =  (!profile.contains("prd")) ? TB.MID : PRD.MID;
        String hashKey = (!profile.contains("prd")) ? TB.HASH_KEY : PRD.HASH_KEY;
        String mTrdNo = (String) resultObj.get("mTrdNo");
        String trdNo = (String) resultObj.get("trdNo");
        String trdAmt = (String) resultObj.get("trdAmt");

        String madePktHashStr = custNo + mId + mTrdNo;
        switch (trdDivCd){
            case "UC":
                break;
            case "CU"://거래금액, 머니금액, 포인트 금액, 해시키
                madePktHashStr += (trdAmt + resultObj.get("mnyAmt") + resultObj.get("pntAmt"));
            case "CUE": //각각사용 임시
                madePktHashStr += (trdAmt + resultObj.get("mnyAmt") + resultObj.get("pntAmt"));
            case "MP":
            case "PP":
            case "MW":
            case "MG":
            case "MC":
            case "PC":
            case "WW"://선불회원번호,상점아이디,상점거래번호,거래금액
                madePktHashStr += trdAmt;
                break;
        }
        madePktHashStr += hashKey;
        try {
            madePktHashStr = digestSHA256(madePktHashStr);
            if (madePktHashStr.equals(pktHashStr)) {

            }else{
                System.out.println("pkt 검증 실패::: 만들어진 값 => [" + madePktHashStr + "]");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 조회성 API 의 경우, responseEntity 응답 전달
     *
     * @param responseEntity
     * @return
     */
    public static Object decodedDto(ResponseEntity<Object> responseEntity) {
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return null;
        }
        Map<String, Object> param = (Map<String, Object>) responseEntity.getBody();
        Map<String, Object> remap = new HashMap<>();

        if (!param.get(RSLT_CD).toString().equals("0000")) {
            log.info("*** API RESPONSE => {}:{}", ((Map<String, Object>) responseEntity.getBody()).get(RSLT_CD), ((Map<String, Object>) responseEntity.getBody()).get(RSLT_MSG));
            remap.put(RSLT_CD, (String) param.get(RSLT_CD));
            remap.put(RSLT_MSG, (String) param.get(RSLT_MSG));
            return remap;
        }
        return param.get(RSLT_OBJ);
    }

    /**
     * 로직성 API 의 경우, pktHash 까지 검증
     *
     * @param responseEntity
     * @param trdDivCd
     * @return
     */
    public static Object decodedDto(String profile, ResponseEntity<Object> responseEntity, String trdDivCd) {
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return null;
        }
        Map<String, Object> param = (Map<String, Object>) responseEntity.getBody();
        Map<String, Object> remap = new HashMap<>();

        if (!param.get(RSLT_CD).toString().equals("0000")) {
            log.info("*** [{}서버] API RESPONSE => {}:{}", profile,((Map<String, Object>) responseEntity.getBody()).get(RSLT_CD), ((Map<String, Object>) responseEntity.getBody()).get(RSLT_MSG));
            remap.put(RSLT_CD, (String) param.get(RSLT_CD));
            remap.put(RSLT_MSG, (String) param.get(RSLT_MSG));
            return remap;
        }
//        validatePktHash(profile, (Map<String, Object>) param.get(RSLT_OBJ), trdDivCd);

        return param.get(RSLT_OBJ);
    }
}
