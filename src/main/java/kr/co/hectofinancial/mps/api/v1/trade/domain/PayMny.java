package kr.co.hectofinancial.mps.api.v1.trade.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "PM_MPS_PAY_MNY", schema = "MPS")
@IdClass(PayMnyPK.class)
public class PayMny {

    @Id
    @Column(name = "LDGR_NO")
    private String ldgrNo;

    @Id
    @Column(name = "MAKE_DT")
    private String makeDt;

    @Column(name = "MAKE_TM")
    private String makeTm;

    @Column(name = "M_ID")
    private String mid;

    @Column(name = "MPS_CUST_NO")
    private String mpsCustNo;

    @Column(name = "CHRG_MEAN_CD")
    private String chrgMeanCd;

    @Column(name = "MNY_AMT")
    private long mnyAmt;

    @Column(name = "PAY_TRD_NO")
    private String payTrdNo;

    @Column(name = "PAY_TRD_DT")
    private String payTrdDt;

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
}
