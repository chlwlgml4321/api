package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PM_MPS_GC_DSTB_TRD", schema = "MPS")
@IdClass(GiftCardDistributorTradePk.class)
public class GiftCardDistributorTrade {

    @Id
    @Column(name = "DSTB_TRD_NO", nullable = false)
    private String dstbTrdNo;

    @Id
    @Column(name = "TRD_DT", nullable = false)
    private String trdDt;

    @Column(name = "TRD_TM", nullable = false)
    private String trdTm;

    @Column(name = "TRD_DIV_CD", nullable = false)
    private String trdDivCd;

    @Column(name = "AMT_SIGN", nullable = false)
    private int amtSign;

    @Column(name = "TRG_AMT", nullable = false)
    private long trgAmt;

    @Column(name = "SVC_CD")
    private String svcCd;

    @Column(name = "PRDT_CD")
    private String prdtCd;

    @Column(name = "M_ID")
    private String mId;

    @Column(name = "M_TRD_NO")
    private String mTrdNo;

    @Column(name = "CHRG_MEAN_CD")
    private String chrgMeanCd;

    @Column(name = "CHRG_TRD_NO")
    private String chrgTrdNo;

    @Column(name = "CNCL_YN")
    private String cnclYn;

    @Column(name = "GC_DSTB_NO", nullable = false)
    private String gcDstbNo;

    @Column(name = "BNDL_PIN_NO_ENC")
    private String bndlPinNoEnc;

    @Column(name = "ORG_DSTB_TRD_NO")
    private String orgDstbTrdNo;

    @Column(name = "ORG_TRD_DT")
    private String orgTrdDt;

    @Column(name = "M_REQ_DTM")
    private String mReqDtm;

    @Column(name = "TRD_SUMRY")
    private String trdSumry;

    @Column(name = "M_RESRV_FIELD_1")
    private String mResrvField1;

    @Column(name = "M_RESRV_FIELD_2")
    private String mResrvField2;

    @Column(name = "M_RESRV_FIELD_3")
    private String mResrvField3;

    @Column(name = "RMK")
    private String rmk;

    @CreatedDate
    @Column(updatable = false, name = "INST_DATE", nullable = false)
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime createdDate;

    @Column(updatable = false, name = "INST_ID", nullable = false)
    @NotNull
    private String createdId;

    @Column(updatable = false, name = "INST_IP", nullable = false)
    @NotNull
    private String createdIp;

    @LastModifiedDate
    @Column(insertable = false, name = "UPDT_DATE")
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime modifiedDate;

    @Column(name = "UPDT_ID")
    private String modifiedId;

    @Column(name = "UPDT_IP")
    private String modifiedIp;
}
