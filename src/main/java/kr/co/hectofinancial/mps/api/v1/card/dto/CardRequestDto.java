package kr.co.hectofinancial.mps.api.v1.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoByMidRequestDto;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonLogicalRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CardRequestDto extends CommonInfoByMidRequestDto {
    @NotBlank(message = "거래 금액")
    private String trdAmt;

    @NotBlank(message = "요청일자")
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt;
    @NotBlank(message = "요청시간")
    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm;
    @NotBlank(message = "승인번호")
    @JsonProperty("mTrdNo")
    private String mTrdNo;//승인번호
    @NotBlank(message = "거래구분코드")
    private String trdDivCd;//승인,승인취소
    private String mid = "M2471645";
}