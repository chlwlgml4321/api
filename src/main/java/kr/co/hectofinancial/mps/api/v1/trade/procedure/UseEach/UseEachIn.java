package kr.co.hectofinancial.mps.api.v1.trade.procedure.UseEach;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UseEachIn {
    private String inMpsCustNo;
    private String inTrdDivCd;
    private String inUseTrdNo;
    private String inUseTrdDt;
    private Long inMnyAmt;
    private Long inPntAmt;
    private Long inBlc;
    private String inWorkerID;
    private String inWorkerIP;

}
