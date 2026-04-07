package kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlcWithExpr;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetBlcWithExprOut {

    private Number outResCd;
    private String outResMsg;
    private Number outMnyBlc;
    private Number outPntBlc;
    private Number outWaitMnyBlc;
    private Number outChrgPsbAmt;
    private Number outExprPntAmt;
    private String outUseTrdNo;
    private String outUseTrdDt;
    private String outUseTrdTm;

}
