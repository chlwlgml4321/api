package kr.co.hectofinancial.mps.api.v1.trade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * 상점 거래상세조회 API 의 요청 Dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TradeInfoByMarketRequestDto extends CommonInfoRequestDto {
    @NotBlank(message = "선불 회원 CI 값")
    @EncField
    private String ci;
    @NotBlank(message = "상점거래번호")
    @JsonProperty("mTrdNo")
    private String mTrdNo;

    @NotBlank(message = "조회 기간")
    @DateFormat(pattern = "yyyyMM", message = "조회 기간 형식은 yyyyMM 입니다.")
    private String period;
}
