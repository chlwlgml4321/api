package kr.co.hectofinancial.mps.api.v1.trade.domain;

import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.DateUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "PM_MPS_TRD", schema = "MPS")
@IdClass(TradePK.class)
@Slf4j
public class Trade {

    @Id
    @Column(name = "TRD_NO")
    private String trdNo;
    @Id
    @Column(name = "TRD_DT")
    private String trdDt;
    @Column(name = "TRD_TM")
    private String trdTm;
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
    @Column(name = "WAIT_MNY_AMT")
    private long waitMnyAmt;
    @Column(name = "CUST_BDN_FEE_AMT")
    private Long custBdnFeeAmt;
    @Column(name = "MNY_BLC")
    private long mnyBlc;
    @Column(name = "PNT_BLC")
    private long pntBlc;
    @Column(name = "WAIT_MNY_BLC")
    private long waitMnyBlc;
    @Column(name = "CHRG_MEAN_CD")
    private String chrgMeanCd;//없을 시 "00"
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
    @Column(name = "CNCL_YN")
    private String cnclYn;
    @Column(name = "STOR_CD")
    private String storCd;
    @Column(name = "STOR_NM")
    private String storNm;
    @Column(name="CNCL_TRD_AMT")
    private long cnclTrdAmt;
    @Column(name="CNCL_MNY_AMT")
    private long cnclMnyAmt;
    @Column(name="CNCL_PNT_AMT")
    private long cnclPntAmt;
    @Column(name="LAST_CNCL_DATE")
    private LocalDateTime lastCnclDate;
    @Column(name = "TRD_DIV_DTL_CD")
    private String trdDivDtlCd;
    @Column(name = "M_RESRV_FIELD_1")
    private String mResrvField1;
    @Column(name = "M_RESRV_FIELD_2")
    private String mResrvField2;
    @Column(name = "M_RESRV_FIELD_3")
    private String mResrvField3;
    @Column(name = "CNCL_TYPE_CD")
    private String cnclTypeCd;
    @Column(name = "RMK")
    private String rmk;
    @Column(name = "CARD_MNG_NO")
    private String cardMngNo;
    @Column(name = "GC_REQ_PKT")
    private String gcReqPkt;

    @CreatedDate
    @Column(updatable = false, name = "INST_DATE")
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
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
    public Trade(String trdNo, String trdDt, String trdTm, String trdDivCd, String svcCd, String prdtCd, String mid, String blcUseOrd, long amtSign, long trdAmt, long mnyAmt, long pntAmt, long waitMnyAmt, long custBdnFeeAmt, long mnyBlc, long pntBlc, long waitMnyBlc, String chrgMeanCd, String mReqDtm, String mTrdNo, String mCustId, String stlMId, String mpsCustNo, String rcvMpsCustNo, String trdSumry, String chrgTrdNo, String orgTrdNo, String orgTrdDt, String csrcIssReqYn, String csrcIssStatCd, String csrcApprNo, String csrcApprDtm, String cnclYn, String storCd, String storNm, long cnclMnyAmt, long cnclPntAmt, long cnclTrdAmt, LocalDateTime lastCnclDate,
                 String trdDivDtlCd, String mResrvField1, String mResrvField2, String mResrvField3, String cnclTypeCd, String cardMngNo, String rmk,
                 LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp, String gcReqPkt) {
        this.trdNo = trdNo;
        this.trdDt = trdDt;
        this.trdTm = trdTm;
        this.trdDivCd = trdDivCd;
        this.svcCd = svcCd;
        this.prdtCd = prdtCd;
        this.mid = mid;
        this.blcUseOrd = blcUseOrd;
        this.amtSign = amtSign;
        this.trdAmt = trdAmt;
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
        this.mCustId = mCustId;
        this.stlMId = stlMId;
        this.mpsCustNo = mpsCustNo;
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
        this.storCd = storCd;
        this.storNm = storNm;
        this.cnclTrdAmt = cnclTrdAmt;
        this.cnclPntAmt = cnclPntAmt;
        this.cnclMnyAmt = cnclMnyAmt;
        this.lastCnclDate = lastCnclDate;
        this.trdDivDtlCd = trdDivDtlCd;
        this.mResrvField1 = mResrvField1;
        this.mResrvField2 = mResrvField2;
        this.mResrvField3 = mResrvField3;
        this.cnclTypeCd = cnclTypeCd;
        this.cardMngNo = cardMngNo;
        this.rmk = rmk;
        this.gcReqPkt = gcReqPkt;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }

    @PrePersist
    public void prePersist() {
        this.cnclYn = this.cnclYn == null ? "N" : this.cnclYn;
        this.csrcIssReqYn = this.csrcIssReqYn == null ? "N" : this.csrcIssReqYn;
        this.custBdnFeeAmt = this.custBdnFeeAmt == null ? 0L : this.custBdnFeeAmt;
        this.blcUseOrd = this.blcUseOrd == null ? "P" : this.blcUseOrd;
    }

    /**
     *
     * @param cnclMnyAmt 취소 머니 금액
     * @param cnclPntAmt 취소 포인트 금액
     * @param waitMnyAmt 대기머니 금액
     *                   cnclMnyAmt + cnclPntAmt + waitMnyAmt = cnclTrdAmt
     * @param cnclDtTm 마지막 취소 일시
     */
    public void orgTradeUpdate(long cnclMnyAmt, long cnclPntAmt, long waitMnyAmt, String cnclDtTm) {
        this.cnclMnyAmt += (cnclMnyAmt + waitMnyAmt);
        this.cnclPntAmt += cnclPntAmt;
        this.cnclTrdAmt += (cnclMnyAmt + cnclPntAmt + waitMnyAmt);
        this.lastCnclDate = DateTimeUtil.convertStringToLocalDateTime(cnclDtTm);
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;

        if (this.cnclTrdAmt > this.trdAmt) {
            //취소 거래 금액이 취소 금액보다 클 수 없으므로 에러
            throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
        }
    }

    public void retryWdTradeUpdate(String chrgTrdNo) {
        this.chrgTrdNo = chrgTrdNo;
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }

//    @PostPersist
//    public void postPersist(){
//        log.debug("**** [PrePersist] 거래테이블 바인딩 변수 출력 => {}", this.toString());
//    }
}
