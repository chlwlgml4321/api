package kr.co.hectofinancial.mps.api.v1.trade.procedure.UseCancel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UseCancelIn {
    private String inMpsCustNo;
    private String inTrdDivCd;
    private String inCustDivCd;
    private Long inChrgLmtAmt;
    private String inUseTrdNo;
    private String inUseTrdDt;
    private String inCnclTrdNo;
    private String inCnclTrdDt;
    private Long inMnyAmt;
    private Long inPntAmt;
    private Long inBlc;
    private String inMID;
    private String inWorkerID;
    private String inWorkerIP;
}
