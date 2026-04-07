package kr.co.hectofinancial.mps.api.v1.trade.procedure.Use;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UseIn {
    private String inMpsCustNo;
    private String inTrdDivCd;
    private String inBlcUseOrd;
    private String inUseTrdNo;
    private String inUseTrdDt;
    private Long inTrdAmt;
    private Long inBlc;
    private String inWorkerID;
    private String inWorkerIP;

}
