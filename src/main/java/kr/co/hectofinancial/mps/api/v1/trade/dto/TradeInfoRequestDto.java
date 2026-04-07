package kr.co.hectofinancial.mps.api.v1.trade.dto;

import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * 거래상세조회 API 의 요청 Dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TradeInfoRequestDto extends CommonInfoRequestDto {
    @NotBlank(message = "선불 회원 CI 값")
    @EncField
    private String ci;
    @NotBlank(message = "거래승인번호")
    private String trdNo;

    @NotBlank(message = "거래승인일자")
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String trdDt;

}
