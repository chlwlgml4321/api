package kr.co.hectofinancial.mps.api.v1.trade.procedure.PayCancel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayCancelIn {

    private String inMpsCustNo;
    private String inTrdDivCd;//거래구분코드
    private String inPayTrdNo;
    private String inPayTrdDt;
    private String inUseTrNo;
    private String inUseTrDt;
    private Long inTrdAmt; //거래금액
    private Long inBlc; //잔액
    private String inWorkerID;
    private String inWorkerIP;

}
