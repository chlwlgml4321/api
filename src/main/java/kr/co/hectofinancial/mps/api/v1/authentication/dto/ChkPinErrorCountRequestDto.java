package kr.co.hectofinancial.mps.api.v1.authentication.dto;

import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarket;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ChkPinErrorCountRequestDto {

    private String pin;//핀번호
    private String trdNo;//거래 번호
    private CustomerDto customerDto;
    private MpsMarket mpsMarket;

    public ChkPinErrorCountRequestDto setFakePassword() {
        this.pin = "000000";
        return this;
    }
}
