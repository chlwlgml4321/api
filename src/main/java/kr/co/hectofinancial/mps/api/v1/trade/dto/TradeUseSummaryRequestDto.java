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
 * 사용 거래 내역 금액 합계 api RequestDto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TradeUseSummaryRequestDto extends CommonInfoRequestDto {
    @NotBlank(message = "선불 회원 CI 값")
    @EncField
    private String ci;
    @DateFormat(pattern = "yyyyMM", message = "조회 기간 형식은 yyyyMM 입니다.")
    private String period;
    @JsonProperty("cardTrdOnlyYn")
    private String cardTrdOnlyYn; //카드거래건만
}
