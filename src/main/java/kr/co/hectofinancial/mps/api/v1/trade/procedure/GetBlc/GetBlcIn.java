package kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlc;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetBlcIn {
    private String inMpsCustNo;
    private Number inChrgLmtAmt;

}
