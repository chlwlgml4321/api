package kr.co.hectofinancial.mps.api.v1.common.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 실시간 펌뱅킹 전문 응답
 *  1. 예금거래명세통지 전문
 */
@Getter
@Builder
public class FirmResponseDto {

    public String data;
}
