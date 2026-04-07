package kr.co.hectofinancial.mps.api.v1.trade.dto.money;

import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.CustomerWalletResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MoneyGiftReceiveRequestDto{

    public String mTrdNo;
    public String trdNo;
    public CustomerDto resCustDto;
    public CustomerDto custDto;
    public CustomerWalletResponseDto resCustWallet;
    private long trdAmt;
    private String trdDt;    //요청 일자
    private String trdTm;    //요청 시각
}