package kr.co.hectofinancial.mps.global.constant;

import kr.co.hectofinancial.mps.global.error.ErrorCode;

public enum PinVerifyResult {
    SUCCESS("성공", ErrorCode.SUCCESS),
    FAIL("불일치", ErrorCode.PIN_NOT_MATCHED),
    LOCKED("계정 잠김", ErrorCode.PIN_NOT_MATCHED),
    ERROR("기타 실패 응답", ErrorCode.WHITELABEL_ERROR),
    CONNECTION_ERROR("연동 오류",ErrorCode.WHITELABEL_CONNECTION_FAIL),
    FAIL_CNT_ERROR("실패횟수 확인 불가",ErrorCode.WHITELABEL_OTHER_ERROR),
    INVALID_ENCRYPTED_PIN("복호화 오류",ErrorCode.PIN_NOT_MATCHED)
    ;

    private final String resultMsg;
    private final ErrorCode errorCode;  //거래실패내역에 쌓을 실제 에러코드

    PinVerifyResult(String resultMsg, ErrorCode errorCode) {
        this.resultMsg = resultMsg;
        this.errorCode = errorCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
