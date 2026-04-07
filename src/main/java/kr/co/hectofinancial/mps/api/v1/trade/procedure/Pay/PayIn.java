package kr.co.hectofinancial.mps.api.v1.trade.procedure.Pay;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayIn {

    private String inMpsCustNo;
    private String inChrgMeanCd;//충전수단코드
    private String inTrdDivCd;//거래구분코드
    private String inTrdDivDtlCd;
    private String inCustDivCd;
    private Long inChrgLmtAmt;
    private String inMID;
    private String inPayTrdNo;
    private String inPayTrdDt;
    private Long inTrdAmt; //거래금액
    private Long inBlc; //잔액
    private String inVldPd; //포인트만료일자
    private String inPntId;
    private String inPayRsn;
    private String inWorkerID;
    private String inWorkerIP;

}
