package kr.co.hectofinancial.mps.api.v1.trade.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "PM_MPS_TRD_FAIL", schema = "MPS")
@IdClass(TrdFailPK.class)
public class TrdFail {

    @Id
    @Column(name = "TRD_NO")
    private String trdNo;
    @Id
    @Column(name = "FAIL_DT")
    private String failDt;
    @Column(name = "FAIL_TM")
    private String failTm;
    @Column(name = "TRD_DIV_CD")
    private String trdDivCd;
    @Column(name = "SVC_CD")
    private String svcCd;
    @Column(name = "PRDT_CD")
    private String prdtCd;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "BLC_USE_ORD")
    private String blcUseOrd;
    @Column(name = "AMT_SIGN")
    private long amtSign;
    @Column(name = "TRD_AMT")
    private long trdAmt;
    @Column(name = "MNY_AMT")
    private long mnyAmt;
    @Column(name = "PNT_AMT")
    private long pntAmt;
    @Column(name = "MNY_BLC")
    private long mnyBlc;
    @Column(name = "PNT_BLC")
    private long pntBlc;
    @Column(name = "WAIT_MNY_BLC")
    private long waitMnyBlc;
    @Column(name = "CUST_BDN_FEE_AMT")
    private Long custBdnFeeAmt;
    @Column(name = "CHRG_MEAN_CD")
    private String chrgMeanCd;
    @Column(name = "M_REQ_DTM")
    private String mReqDtm;
    @Column(name = "M_TRD_NO")
    private String mTrdNo;
    @Column(name = "M_CUST_ID")
    private String mCustId;
    @Column(name = "STL_M_ID")
    private String stlMId;
    @Column(name = "MPS_CUST_NO")
    private String mpsCustNo;
    @Column(name = "RCV_MPS_CUST_NO")
    private String rcvMpsCustNo;
    @Column(name = "TRD_SUMRY")
    private String trdSumry;
    @Column(name = "CHRG_TRD_NO")
    private String chrgTrdNo;
    @Column(name = "ORG_TRD_NO")
    private String orgTrdNo;
    @Column(name = "ORG_TRD_DT")
    private String orgTrdDt;
    @Column(name = "CSRC_ISS_REQ_YN")
    private String csrcIssReqYn;
    @Column(name = "CSRC_ISS_STAT_CD")
    private String csrcIssStatCd;
    @Column(name = "CSRC_APPR_NO")
    private String csrcApprNo;
    @Column(name = "CSRC_APPR_DTM")
    private String csrcApprDtm;
    @Column(name = "WAIT_MNY_AMT")
    private long waitMnyAmt;
    @Column(name = "ERR_CD")
    private String errCd;
    @Column(name = "ERR_MSG")
    private String errMsg;
    @Column(name = "CNCL_YN")
    private String cnclYn;
    @Column(name = "STOR_CD")
    private String storCd;
    @Column(name = "STOR_NM")
    private String storNm;
    @Column(name = "TRD_DIV_DTL_CD")
    private String trdDivDtlCd;
    @Column(name = "M_RESRV_FIELD_1")
    private String mResrvField1;
    @Column(name = "M_RESRV_FIELD_2")
    private String mResrvField2;
    @Column(name = "M_RESRV_FIELD_3")
    private String mResrvField3;
    @Column(name = "CARD_MNG_NO")
    private String cardMngNo;
    @Column(name = "GC_REQ_PKT")
    private String gcReqPkt;

    @CreatedDate
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    @Column(updatable = false, name = "INST_DATE")
    private LocalDateTime createdDate;

    @Column(updatable = false, name = "INST_ID")
    private String createdId;

    @Column(updatable = false, name = "INST_IP")
    private String createdIp;

    @LastModifiedDate
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    @Column(insertable = false, name = "UPDT_DATE")
    private LocalDateTime modifiedDate;

    @Column(updatable = true, name = "UPDT_ID")
    private String modifiedId;

    @Column(updatable = true, name = "UPDT_IP")
    private String modifiedIp;


    @Builder
    public TrdFail(String trdNo, String failDt, String failTm, long trdAmt, String trdDivCd, String svcCd, String prdtCd, String mid, String blcUseOrd, long amtSign, long mnyAmt, long pntAmt,
                   long mnyBlc, long pntBlc, long waitMnyBlc, String chrgMeanCd, String mReqDtm, String mTrdNo, String mCustId, String stlMId, String mpsCustNo, String rcvMpsCustNo, String trdSumry, long custBdnFeeAmt,
                   String chrgTrdNo, String orgTrdNo, String orgTrdDt, String csrcIssReqYn, String csrcIssStatCd, String csrcApprNo, String csrcApprDtm, long waitMnyAmt, String cnclYn,
                   String trdDivDtlCd, String mResrvField1, String mResrvField2, String mResrvField3, String cardMngNo,
                   LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp, String errCd, String errMsg, String storCd, String storNm,
                   long cnclMnyAmt, long cnclPntAmt, long cnclTrdAmt, LocalDateTime lastCnclDate, String gcReqPkt) {
        this.trdNo = trdNo;
        this.failDt = failDt;
        this.failTm = failTm;
        this.trdAmt = trdAmt;
        this.trdDivCd = trdDivCd;
        this.svcCd = svcCd;
        this.prdtCd = prdtCd;
        this.mid = mid;
        this.blcUseOrd = blcUseOrd;
        this.amtSign = amtSign;
        this.mnyAmt = mnyAmt;
        this.pntAmt = pntAmt;
        this.waitMnyAmt = waitMnyAmt;
        this.custBdnFeeAmt = custBdnFeeAmt;
        this.mnyBlc = mnyBlc;
        this.pntBlc = pntBlc;
        this.waitMnyBlc = waitMnyBlc;
        this.chrgMeanCd = chrgMeanCd;
        this.mReqDtm = mReqDtm;
        this.mTrdNo = mTrdNo;
        this.mpsCustNo = mpsCustNo;
        this.mCustId = mCustId;
        this.stlMId = stlMId;
        this.rcvMpsCustNo = rcvMpsCustNo;
        this.trdSumry = trdSumry;
        this.chrgTrdNo = chrgTrdNo;
        this.orgTrdNo = orgTrdNo;
        this.orgTrdDt = orgTrdDt;
        this.csrcIssReqYn = csrcIssReqYn;
        this.csrcIssStatCd = csrcIssStatCd;
        this.csrcApprNo = csrcApprNo;
        this.csrcApprDtm = csrcApprDtm;
        this.cnclYn = cnclYn;
        this.trdDivDtlCd = trdDivDtlCd;
        this.mResrvField1 = mResrvField1;
        this.mResrvField2= mResrvField2;
        this.mResrvField3 = mResrvField3;
        this.cardMngNo = cardMngNo;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
        this.errCd = errCd;
        this.errMsg = errMsg;
        this.storCd = storCd;
        this.storNm = storNm;
        this.gcReqPkt = gcReqPkt;
    }
    @PrePersist
    public void prePersist() {
        this.cnclYn = this.cnclYn == null ? "N" : this.cnclYn;
        this.csrcIssReqYn = this.csrcIssReqYn == null ? "N" : this.csrcIssReqYn;
        this.custBdnFeeAmt = this.custBdnFeeAmt == null ? 0L : this.custBdnFeeAmt;
        this.storCd = this.svcCd == null? "N" : this.storCd;
        this.storNm = this.storNm == null? "N" : this.storNm;
    }
}
