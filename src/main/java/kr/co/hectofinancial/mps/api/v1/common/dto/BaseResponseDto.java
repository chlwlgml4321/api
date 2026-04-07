package kr.co.hectofinancial.mps.api.v1.common.dto;

import lombok.*;

/**
 * Controller 의 ResponseEntity 안에 들어가는 공통 응답 Entity
 * 조회성, 로직성 API 응답에 공통적으로 사용됨
 */
@Getter
@RequiredArgsConstructor
@Setter
public class BaseResponseDto {

    public String rsltCd;
    public String rsltMsg;
    public Object rsltObj;

    @Builder
    public BaseResponseDto(String rsltCd, String rsltMsg, Object rsltObj) {
        this.rsltCd = rsltCd;
        this.rsltMsg = rsltMsg;
        this.rsltObj = rsltObj;
    }
}
