package kr.co.hectofinancial.mps.global.mtms;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class TmsAgent {
    private static final Logger tmsLogger = LoggerFactory.getLogger("tmsAgent");
    private static final String FAIL_CD = "9999";
    private static final String SUCC_CD = "0000";

    /**
     *
     * @param mId
     * @param pktType
     * @param ornId
     * @param clientIp
     * @param errorCd
     * @param elapsedTime
     * @param trdAmt
     * @param trdNo
     * @description 거래/오류 급증 캐치하기 위한 AI MON
     * <p>
     * 로그 양식 : 시간  REQ_TYPE[1] SVC_CD[MMPS] MID[] PKT_TYPE[MNG] ORN_ID[HF] CLIENT_IP[192.168.0.115] RSLT_CD[0000] ELAPSED_MSEC[0] AMT[0] GLOBAL_ID[REQ_NO]
     * _req_type    : 0(가맹점요청), 1(당사응답)
     * _mid         : M_ID (max 20 bytes) -- 상점아이디
     * _pkt_type    : RP, RT 등 (max 8 bytes) --상품코드or전문타입
     * _orn_id      : 원천사아이디 (max 10 bytes), 오픈뱅킹은 KFT.
     * _client_ip   : Client IP (max 20 bytes)
     * _rslt_cd     : OUT_RSLT_CD (max 6 bytes)
     * _elapsed_msec: 처리소요시간. 1초 = 1000 (max 7 bytes)
     * _amt         : 거래금액 (max 10 bytes)
     * _glb_id      : 글로벌 ID     (max 32 bytes)
     * </p>
     */
    public static void sendTmsAgent(String mId, String pktType, String clientIp, String errorCd, String elapsedTime, String trdAmt, String trdNo) {
        //ornId = mId 같이 사용
        try {
            log.info("TMS 로그 진입 ===========> MID[{}] PKT_TYPE[{}] ORN_ID[{}] CLIENT_IP[{}] RSLT_CD[{}] ELAPSED_MSEC[{}] AMT[{}] GLOBAL_ID[{}]"
                    , mId, pktType, mId, clientIp, errorCd, elapsedTime, trdAmt, trdNo);
            tmsLogger.info("MID[{}] PKT_TYPE[{}] ORN_ID[{}] CLIENT_IP[{}] RSLT_CD[{}] ELAPSED_MSEC[{}] AMT[{}] GLOBAL_ID[{}]"
                    , mId, pktType, mId, clientIp, errorCd, elapsedTime, trdAmt, trdNo);
        } catch (Exception e) {
            log.info("TMS 로그 찍는 도중 에러 발생, {}", e.getStackTrace());
        }
    }

}
