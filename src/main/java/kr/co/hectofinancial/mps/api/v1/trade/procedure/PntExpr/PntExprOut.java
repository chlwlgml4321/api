package kr.co.hectofinancial.mps.api.v1.trade.procedure.PntExpr;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PntExprOut {

    private Number outResCd;
    private String outResMsg;
    private Number outMnyBlc;
    private Number outPntBlc;
    private Number outExprPntAmt;
    private Number outWaitMnyBlc;
}