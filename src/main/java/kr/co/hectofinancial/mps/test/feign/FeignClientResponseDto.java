package kr.co.hectofinancial.mps.test.feign;

import lombok.Data;

@Data
public class FeignClientResponseDto {
    private String rsltCd;
    private String rsltMsg;
    private Object rsltObj;

}

