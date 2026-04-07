package kr.co.hectofinancial.mps.api.v1.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 로직성 Api (사용, 사용취소, 지급, 지급취소 등) 의 응답값 내 공통값을 관리하기 위해 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CommonLogicalResponseDto {
    public String pktHash;
}
