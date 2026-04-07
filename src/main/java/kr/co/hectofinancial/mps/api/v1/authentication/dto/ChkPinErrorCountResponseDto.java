package kr.co.hectofinancial.mps.api.v1.authentication.dto;

import kr.co.hectofinancial.mps.global.constant.PinVerifyResult;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class ChkPinErrorCountResponseDto {

    private PinVerifyResult pinVerifyResult;
    private long failCnt = 0;
    private String customErrorMsg = "";

    public ChkPinErrorCountResponseDto(PinVerifyResult pinVerifyResult) {
        this.pinVerifyResult = pinVerifyResult;

        log.info("ChkPinErrorCountResponseDto result=>{} message=>{}", pinVerifyResult.name(), pinVerifyResult.getResultMsg());
    }
    public ChkPinErrorCountResponseDto(PinVerifyResult pinVerifyResult, long failCnt) {
        this.pinVerifyResult = pinVerifyResult;
        this.failCnt = failCnt;

        log.info("ChkPinErrorCountResponseDto result=>{} message=>{} failCnt=>{}", pinVerifyResult.name(), pinVerifyResult.getResultMsg(), failCnt);
    }
    public ChkPinErrorCountResponseDto(PinVerifyResult pinVerifyResult, String customErrorMsg) {
        this.pinVerifyResult = pinVerifyResult;
        this.customErrorMsg = customErrorMsg;

        log.info("ChkPinErrorCountResponseDto result=>{} message=>{} customErrorMsg=>{}", pinVerifyResult.name(), pinVerifyResult.getResultMsg(), customErrorMsg);
    }
    /**
     * API Response 안에 담길 결제 비밀번호 관련 에러메세지
     * @return
     */
    public String getPinVerifyResultMsg() {
        String msg = "";

        if (pinVerifyResult.equals(PinVerifyResult.FAIL)) {
            msg = "(" + PinVerifyResult.FAIL.getResultMsg() + " 횟수:" + failCnt + ")";
        }else if(pinVerifyResult.equals(PinVerifyResult.LOCKED)) {
            msg = "(" + PinVerifyResult.LOCKED.getResultMsg() + ")";
        }else if(pinVerifyResult.equals(PinVerifyResult.INVALID_ENCRYPTED_PIN)) {
            msg = "(" + PinVerifyResult.INVALID_ENCRYPTED_PIN.getResultMsg() + ")";
        }else {
            //위의 결제비밀번호 불일치 관련된 에러를 제외하고는 MTMS 전송 뒤, 아래와 같은 응답으로 나간다
            //고객에게 보여지는 응답을 PIN_NOT_MATCHED -> WHITE_LABEL_CONNECTION_EXCEPTION 로 변경 (26/01/13)

            if (pinVerifyResult.equals(PinVerifyResult.CONNECTION_ERROR)) {
                msg = "(" + PinVerifyResult.CONNECTION_ERROR.getResultMsg() + ")";
            } else if (pinVerifyResult.equals(PinVerifyResult.FAIL_CNT_ERROR)) {
                msg = "(" + PinVerifyResult.FAIL_CNT_ERROR.getResultMsg() + ")";
            } else if (pinVerifyResult.equals(PinVerifyResult.ERROR)) {
                msg = "(" + PinVerifyResult.ERROR.getResultMsg() + " - " + customErrorMsg + ")";
            }
            //MTMS 는 거래실패 테이블에 쌓는 내용대로 오고, API 응답은 잠시후 다시시도해주세요
            MonitAgent.sendMonitAgent(pinVerifyResult.getErrorCode().getErrorCode(), pinVerifyResult.getErrorCode().getErrorMessage() + msg);
            throw new RequestValidationException(ErrorCode.WHITE_LABEL_CONNECTION_EXCEPTION);
        }
        return msg;
    }

    /**
     * MPS.PM_MPS_TRD_FAIL 에 담길 결제 비밀번호 관련 에러메세지
     * @returnspsp
     */
    public String getPinVerifyResultMsgForTrdFail() {
        String msg = "";
        switch (pinVerifyResult) {
            case FAIL:
                msg = "(" + PinVerifyResult.FAIL.getResultMsg() + " 횟수:" + failCnt + ")";break;
            case LOCKED:
                msg = "(" + PinVerifyResult.LOCKED.getResultMsg() + ")";break;
            case CONNECTION_ERROR:
                msg = "(" + PinVerifyResult.CONNECTION_ERROR.getResultMsg() + ")";break;
            case FAIL_CNT_ERROR:
                msg = "(" + PinVerifyResult.FAIL_CNT_ERROR.getResultMsg() + ")";break;
            case ERROR:
                msg = "(" + PinVerifyResult.ERROR.getResultMsg() + " - " + customErrorMsg + ")";break;
            case INVALID_ENCRYPTED_PIN:
                msg = "(" + PinVerifyResult.INVALID_ENCRYPTED_PIN.getResultMsg() + ")";break;
        }
        return msg;
    }
}
