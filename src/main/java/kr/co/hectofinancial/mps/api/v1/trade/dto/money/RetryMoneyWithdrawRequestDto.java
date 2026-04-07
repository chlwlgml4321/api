package kr.co.hectofinancial.mps.api.v1.trade.dto.money;

import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
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

public class RetryMoneyWithdrawRequestDto extends CommonInfoRequestDto {

    @NotBlank(message = "원거래번호")
    public String orgTrdNo;
    @NotBlank(message = "환불거래번호")
    public String rfdTrdNo;
    @NotBlank(message = "원거래일자")
    public String orgTrdDt;

}
