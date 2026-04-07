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
@Table(name = "PM_MPS_GC_ISS", schema = "MPS")
@IdClass(GiftCardIssuePk.class)
public class GiftCardIssue {

    @Id
    @Column(name = "GC_NO_ENC", nullable = false)
    private String gcNoEnc;

    @Id
    @Column(name = "ISS_DT", nullable = false)
    private String issDt;

    @Column(name = "GC_NO_MSK")
    private String gcNoMsk;

    @Column(name = "ISS_TRD_NO")
    private String issTrdNo;

    @Column(name = "ISS_AMT", nullable = false)
    private long issAmt;

    @Column(name = "BLC", nullable = false)
    private long blc;

    @Column(name = "VLD_PD", nullable = false)
    private String vldPd;

    @Column(name = "USE_M_ID", nullable = false)
    private String useMid;

    @Column(name = "GC_STAT_CD", nullable = false)
    private String gcStatCd;

    @Column(name = "BF_GC_NO_ENC")
    private String bfGcNoEnc;

    @Column(name = "BF_ISS_DT")
    private String bfIssDt;

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

    @Column(name = "BNDL_PIN_ISS_DT")
    private String bndlPinIssDt;
}
