package kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto;

import kr.co.hectofinancial.mps.api.v1.card.dto.CardUseApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MpsMarketDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletUseEachRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletUseRequestDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class AutoChargeDto {

    //실패거래건 쌓을때 사용할 변수
    private WalletUseRequestDto walletUseRequestDto;
    private WalletUseEachRequestDto walletUseEachRequestDto;
    private CardUseApprovalRequestDto cardUseApprovalRequestDto;

    //autoCharge에서 사용할 변수
    private String custNo;
    private String mTrdNo;
    private long reqAmt;
    private long mnyBlc;
    private String reqDt;
    private String reqTm;
    private List<CustAutoChargeMethod> chargeMethods;
    private CustomerDto customerDto;
    private MpsMarketDto marketDto;
    private MarketAddInfoDto marketAddInfoDto;

}
