package kr.co.hectofinancial.mps.api.v1.trade.dto.wallet;

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
public class WalletBalanceRequestDto extends CommonInfoRequestDto {
    @EncField(nullable = true)
    @JsonProperty("ci")
    @NotBlank(message = "CI값")
    private String ci;

}
