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
@Table(name = "PM_MPS_GC_BNDL_PIN", schema = "MPS")
@IdClass(GiftCardBundlePinPk.class)
public class GiftCardBundlePin {

    @Id
    @Column(name = "BNDL_PIN_NO_ENC", nullable = false)
    private String bndlPinNoEnc;

    @Id
    @Column(name = "ISS_DT", nullable = false)
    private String issDt;

    @Column(name = "BNDL_PIN_NO_MSK", nullable = false)
    private String bndlPinNoMsk;

    @Column(name = "M_ID", nullable = false)
    private String mId;

    @Column(name = "GC_DSTB_NO", nullable = false)
    private String gcDstbNo;

    @Column(name = "BNDL_AMT", nullable = false)
    private long bndlAmt;

    @Column(name = "BNDL_REQ_DTM", nullable = false)
    private String bndlReqDtm;

    @Column(name = "BNDL_CMPLT_DTM")
    private String bndlCmpltDtm;

    @Column(name = "VLD_PD", nullable = false)
    private String vldPd;

    @Column(name = "PIN_STAT_CD", nullable = false)
    private String pinStatCd;

    @Column(name = "GC_STAT_CD")
    private String gcStatCd;

    @Column(name = "BNDL_REQ_INFO", nullable = false)
    private String bndlReqInfo;

    @Column(name = "BNDL_REQ_NO")
    private String bndlReqNo;

    @Column(name = "BNDL_REQ_DT")
    private String bndlReqDt;

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
