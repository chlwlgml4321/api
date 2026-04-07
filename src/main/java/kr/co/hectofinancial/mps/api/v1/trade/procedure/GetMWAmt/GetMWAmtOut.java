package kr.co.hectofinancial.mps.api.v1.trade.procedure.GetMWAmt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetMWAmtOut {

    private Number outResCd;
    private String outResMsg;
    private Number outMwAmt;
}
