package kr.co.hectofinancial.mps.api.v1.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoByCardRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateParamCardUseCancelApprovalRequestDto extends CommonInfoByCardRequestDto {
    @NotBlank(message = "카드 승인번호")
    @JsonProperty("mTrdNo")
    public String mTrdNo;
    @NotBlank(message = "승인요청일자")
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt;
    @NotBlank(message = "승인요청시간")
    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm;
}
