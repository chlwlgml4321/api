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
@Table(name = "PM_MPS_GC_USE", schema = "MPS")
@IdClass(GiftCardUsePk.class)
public class GiftCardUse {

    @Id
    @Column(name = "GC_NO_ENC", nullable = false)
    private String gcNoEnc;

    @Id
    @Column(name = "ISS_DT", nullable = false)
    private String issDt;

    @Id
    @Column(name = "USE_DATE", nullable = false)
    private LocalDateTime useDate;

    @Column(name = "AMT_SIGN", nullable = false)
    private int amtSign;

    @Column(name = "CNCL_YN", nullable = false)
    private String cnclYn;

    @Column(name = "USE_AMT", nullable = false)
    private long useAmt;

    @Column(name = "BLC", nullable = false)
    private long blc;

    @Column(name = "TRD_NO")
    private String trdNo;

    @Column(name = "TRD_DT")
    private String trdDt;

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
}
