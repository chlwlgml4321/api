package kr.co.hectofinancial.mps.global.mtms;

import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonLogicalRequestDto;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

@Component
public class MonitAgent {


    private static final Logger monitLogger = LoggerFactory.getLogger("monitAgent");

    /**
     * 선불 알람 *** 알림방으로 가는 기본 코드 쌍
     */
    private static final String JOB_CD = "MPS_A";
    private static final String MNG_CD = "E-999";//errorCode

    public static void sendMonitAgent(String errorCd, String errorMsg) {
        sendMonitAgent(JOB_CD, errorCd, errorMsg);
    }


    /**
     * 사업지원팀 알람방으로 가는 코드 쌍
     */
    private static final String JOB_CD_BIZ = "MMPS";

    public static void sendMonitForBiz(String errorCd, String errorMsg) {
        sendMonitAgent(JOB_CD_BIZ, errorCd, errorMsg);
    }

    /**
     * @param errorCd
     * @param methodNm
     * @param glbId
     * @param msg
     * @description 특정 오류에 대한 모니터링을 위한 로그
     * <p>
     * JOB_CD      : 업무(JOB)코드                  max:5 bytes - MPS_A/MPS_M/MMPS
     * MNG_CD      : 관리(MNG)코드                  max:5 bytes - E-001/9999/BIZS
     * module_nm   : 서비스모듈명 또는 로그파일명     max:20 bytes - MPS_API 고정
     * glb_id      : 글로벌 ID                      max:32 bytes - yyyymmddhhmmss 고정
     * err_msg     : 알람으로로 공지할 메시지         max:100 bytes - 메신저에 보여질 메세지 (현재 클래스에서 서버정보 더해져서 알람방으로 전송됨)
     * PID         : 프로세스ID                                    - logback.xml 에 pattern 설정되어 있음
     */

    /**
     * 해당 메서드는 private 으로 돌려서, 해당 클래스에서만 호출하도록 수정 2024-11-15
     */
    private static void sendMonitAgent(String jobCd, String mngCd, String errorMsg) {
        String message = errorMsg + "\n" + ServerInfoConfig.HOST_NAME + "\nContainer Name : " + System.getProperty("jeus.container.name");
        ; //네이버웍스 채팅방 미리보기에서 메세지 어느정도 보이도록 msg 를 앞쪽에 배치 + 뒤에 서버 정보
        String glbId = new CustomDateTimeUtil().getDateTime();
        String moduleNm = "MPS_API";

        monitLogger.info("SECT[{}][{}] ERR[{}][{}][{}]", jobCd, mngCd, moduleNm, glbId, message);
    }


    /**
     * RequestValidationException 을 파라미터로 넘길경우, 해당 exception 에 setting 되어있는 Method Args 를 이용해서 error msg 추가
     *
     * @param ex
     */
    public static void sendMonitAgent(RequestValidationException ex) {
        String errorCd = ex.getErrorCode().getErrorCode();
        String errorMsg = ex.getErrorCode().getErrorMessage();
        Object[] methodArgs = ex.getMedthodArgs();

        if (methodArgs.length > 0) {
            //찾아야 하는 변수
            String mid = null;
            String custNo = null;
            String gcDstbNo = null; //유통관리번호
            String useMid = null; //사용처상점아이디
            for (int i = 0; i < methodArgs.length; i++) {
                Object methodArg = methodArgs[i];
                if (methodArg instanceof CommonLogicalRequestDto) {
                    if (StringUtils.isNotEmpty(((CommonLogicalRequestDto) methodArg).getCustNo())) {
                        custNo = ((CommonLogicalRequestDto) methodArg).getCustNo();
                    }
                    if (((CommonLogicalRequestDto) methodArg).getCustomerDto() != null) {
                        if (StringUtils.isNotEmpty(((CommonLogicalRequestDto) methodArg).getCustomerDto().getMid())) {
                            mid = ((CommonLogicalRequestDto) methodArg).getCustomerDto().getMid();
                        }
                    }
                } else if (methodArg instanceof CommonInfoRequestDto) {
                    if (StringUtils.isNotEmpty(((CommonInfoRequestDto) methodArg).getCustNo())) {
                        custNo = ((CommonInfoRequestDto) methodArg).getCustNo();
                    }
                    if (((CommonInfoRequestDto) methodArg).getCustomerDto() != null) {
                        if (StringUtils.isNotEmpty(((CommonInfoRequestDto) methodArg).getCustomerDto().getMid())) {
                            mid = ((CommonInfoRequestDto) methodArg).getCustomerDto().getMid();
                        }
                    }
                } else {
                    //유통상품권 관련 요청 useMid, gcDstbNo 찾기
                    for (Field field : methodArg.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        String name = field.getName();
                        if ("gcDstbNo".equals(name)) {
                            try {
                                gcDstbNo = (String) field.get(methodArg);
                            } catch (IllegalAccessException ignore) {
                            }
                        }
                        if ("useMid".equals(name)) {
                            try {
                                useMid = (String) field.get(methodArg);
                            } catch (IllegalAccessException ignore) {
                            }
                        }
                    }

                }
                if (StringUtils.isNotEmpty(mid) && StringUtils.isNotEmpty(custNo)
                        || StringUtils.isNotEmpty(gcDstbNo) && StringUtils.isNotEmpty(useMid)
                        //mId랑 mpsCustNo 둘 다 있거나, 유통관리번호랑 useMid 둘 다 있으면 break;
                ) {
                    break;
                }
            }
            if (StringUtils.isNotEmpty(mid)) {
                errorMsg += " mId[" + mid + "]";
            }
            if (StringUtils.isNotEmpty(custNo)) {
                errorMsg += " mpsCustNo[" + custNo + "]";
            }
            if (StringUtils.isNotEmpty(gcDstbNo)) {
                errorMsg += " gcDstbNo[" + gcDstbNo + "]";
            }
            if (StringUtils.isNotEmpty(useMid)) {
                errorMsg += " useMid[" + useMid + "]";
            }
        }
        if ("R".equals(ex.getErrorCode().getMonitFlag())) {
            errorCd = ErrorCode.NO_REFERENCE_DATA.getErrorCode();
            errorMsg = ErrorCode.NO_REFERENCE_DATA.getErrorMessage() + " =>> ORIGINAL ERROR: " + errorMsg;
        }
        sendMonitAgent(errorCd, errorMsg);
    }
}


