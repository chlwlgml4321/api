package kr.co.hectofinancial.mps.api.v1.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CustomerRequestDto extends CommonInfoRequestDto {
    @NotBlank(message = "상점 아이디")
    private String mid;
    @JsonProperty("custId")
    @EncField(nullable = true)
    private String custId;


    @NotBlank(message = "선불 회원 CI 값")
    @EncField
    private String ci;
}
