package kr.co.hectofinancial.mps.global.error.exception;

import lombok.Getter;

@Getter
public class RetryableChargeException extends RuntimeException {
    private final long resCode;
    private final String resMsg;

    public RetryableChargeException(long resCode, String resMsg) {
        this.resCode = resCode;
        this.resMsg = resMsg;
    }
}
