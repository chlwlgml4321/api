package kr.co.hectofinancial.mps.api.v1.trade.dto.money;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class WithdrawalMoneyRequestDto extends CommonInfoRequestDto {
    @EncField(nullable = true)
    @JsonProperty("ci")
    private String ci;

}
