package kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetBlcOut {

    private Number outResCd;
    private String outResMsg;
    private Number outMnyBlc;
    private Number outPntBlc;
    private Number outWaitMnyBlc;
    private Number outChrgPsbAmt;
}
