package kr.co.hectofinancial.mps.api.v1.trade.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class TradeFailInsertDto {

    public String trdNo;
    public String custNo;
    public String mid;
    public String failDt;
    public String failTm;
    public String trdDivCd;
    public long amtSign;
    public long trdAmt;
    public long mnyAmt;
    public long pntAmt;
    public long waitMnyAmt;
    public long mnyBlc;
    public long pntBlc;
    public long waitMnyBlc;
    public String blcUseOrd;
    public String chrgMeanCd;
    public String reqDtm;
    public String mTrdNo;
    public String trdSumry;
    public String chrgTrdNo;
    public String orgTrDt;
    public String orgTrNo;
    public String errCd;
    public String errMsg;
    public String csrcIssReqYn;
    public String csrcIssStatCd;
    public long custBdnFeeAmt;
    public String stlMId;//정산 상점 아이디
    public String storCd;
    public String storNm;
    public String prdtCd;
    public String rcvMpsCustNo;
    public String mCustId;
    public String trdDivDtlCd;
    public String mResrvField1;
    public String mResrvField2;
    public String mResrvField3;
    public String cardMngNo;
    public String gcPktReq;
}
