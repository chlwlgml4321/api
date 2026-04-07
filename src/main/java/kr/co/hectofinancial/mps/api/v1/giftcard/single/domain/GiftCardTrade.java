package kr.co.hectofinancial.mps.api.v1.giftcard.single.domain;

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
@Table(name = "PM_MPS_GC_TRD", schema = "MPS")
@IdClass(GiftCardTradePk.class)
public class GiftCardTrade {

    @Id
    @Column(name = "TRD_NO", nullable = false)
    private String trdNo;

    @Id
    @Column(name = "TRD_DT", nullable = false)
    private String trdDt;

    @Column(name = "TRD_TM", nullable = false)
    private String trdTm;

    @Column(name = "SVC_CD", nullable = false)
    private String svcCd;

    @Column(name = "PRDT_CD", nullable = false)
    private String prdtCd;

    @Column(name = "USE_M_ID", nullable = false)
    private String useMid;

    @Column(name = "STL_M_ID")
    private String stlMid;

    @Column(name = "AMT_SIGN", nullable = false)
    private int amtSign;

    @Column(name = "CNCL_YN", nullable = false)
    private String cnclYn;

    @Column(name = "TRD_AMT", nullable = false)
    private long trdAmt;

    @Column(name = "ORG_TRD_NO")
    private String orgTrdNo;

    @Column(name = "ORG_TRD_DT")
    private String orgTrdDt;

    @Column(name = "GC_LIST")
    private String gcList;

    @Column(name = "M_REQ_DTM", nullable = false)
    private String mReqDtm;

    @Column(name = "M_TRD_NO", nullable = false)
    private String mTrdNo;

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
    @Column(updatable = false, name = "INST_DATE")
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime createdDate;

    @Column(updatable = false, name = "INST_ID")
    @NotNull
    private String createdId;

    @Column(updatable = false, name = "INST_IP")
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

    @Column(name = "BNDL_PIN_NO_ENC")
    private String bndlPinNoEnc;
}
