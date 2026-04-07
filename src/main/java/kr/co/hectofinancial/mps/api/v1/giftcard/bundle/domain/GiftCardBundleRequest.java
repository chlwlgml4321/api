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
@Table(name = "PM_MPS_GC_BNDL_REQ", schema = "MPS")
@IdClass(GiftCardBundleRequestPk.class)
public class GiftCardBundleRequest {

    @Id
    @Column(name = "REQ_NO", nullable = false)
    private String reqNo;

    @Id
    @Column(name = "REQ_DT", nullable = false)
    private String reqDt;

    @Column(name = "REQ_TM", nullable = false)
    private String reqTm;

    @Column(name = "M_ID", nullable = false)
    private String mId;

    @Column(name = "GC_DSTB_NO", nullable = false)
    private String gcDstbNo;

    @Column(name = "REQ_AMT", nullable = false)
    private String reqAmt;

    @Column(name = "REQ_INFO", nullable = false)
    private String reqInfo;

    @Column(name = "M_REQ_DTM", nullable = false)
    private String mReqDtm;

    @Column(name = "M_TRD_NO", nullable = false)
    private String mTrdNo;

    @Column(name = "RSLT_CD")
    private String rsltCd;

    @Column(name = "RSLT_MSG")
    private String rsltMsg;

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
