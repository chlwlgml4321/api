package kr.co.hectofinancial.mps.global.error;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private String rsltCd;
    private String rsltMsg;

    public static ErrorResponse of(String errorCode, String errorMessage) {
        return ErrorResponse.builder()
                .rsltCd(errorCode)
                .rsltMsg(errorMessage)
                .build();
    }

}
