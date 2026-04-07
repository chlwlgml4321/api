package kr.co.hectofinancial.mps.global.error.exception;

import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.Getter;

@Getter
public class RequestValidationException extends RuntimeException {
    private final ErrorCode errorCode;
    private Object[] medthodArgs;
    private String additionalErrorMsg;

    public RequestValidationException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public RequestValidationException(ErrorCode errorCode, String additionalErrorMsg) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
        this.additionalErrorMsg = additionalErrorMsg;
    }

    public Object[] getMedthodArgs() {
        return medthodArgs;
    }

    public void setMedthodArgs(Object[] medthodArgs) {
        this.medthodArgs = medthodArgs;
    }
}
