package kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlcWithExpr;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetBlcWithExprIn {
    private String inMpsCustNo;
    private Number inChrgLmtAmt;
    private String inWorkerId;
    private String inWorkerIp;

}
