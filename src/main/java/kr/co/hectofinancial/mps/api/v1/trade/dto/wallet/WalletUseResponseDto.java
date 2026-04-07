package kr.co.hectofinancial.mps.api.v1.trade.dto.wallet;

import kr.co.hectofinancial.mps.api.v1.common.dto.CommonLogicalResponseDto;
import kr.co.hectofinancial.mps.global.annotation.HashField;
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
public class WalletUseResponseDto extends CommonLogicalResponseDto {
    @HashField(order = 1)
    private String custNo;
    @HashField(order = 2)
    private String mTrdNo;
    @HashField(order = 3)
    private String trdNo;
    private String trdDt;
    private String trdTm;
    @HashField(order = 4)
    private String trdAmt;
    @HashField(order = 5)
    private String mnyAmt;
    @HashField(order = 6)
    private String pntAmt;

    private String mnyBlc;
    private String pntBlc;


}
