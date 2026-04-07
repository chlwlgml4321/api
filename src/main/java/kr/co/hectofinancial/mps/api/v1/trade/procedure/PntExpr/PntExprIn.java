package kr.co.hectofinancial.mps.api.v1.trade.procedure.PntExpr;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PntExprIn {
    private String inMpsCustNo;
    private String inTrdDivCd;
    private String inUseTrdNo;
    private String inUseTrdDt;
    private String inCnclTrdNo;
    private String inCnclTrdDt;
    private Number inTrdAmt;
    private String inWorkerID;
    private String inWorkerIP;
}
