package kr.co.hectofinancial.mps.api.v1.trade.procedure.GetMWAmt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetMWAmtIn {
    private String inMpsCustNo;
}
