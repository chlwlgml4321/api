package kr.co.hectofinancial.mps.api.v1.authentication.dto;

import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetBillKeyRequestDto extends CommonInfoRequestDto {

    @NotBlank(message = "상점 아이디")
    private String mid;
    @NotBlank(message = "선불 회원 아이디")
    @EncField
    private String custId;
    @NotBlank(message = "식별자 값, CI OR 사업자번호")
    @EncField
    public String identifier;
}
