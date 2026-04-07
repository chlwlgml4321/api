package kr.co.hectofinancial.mps.api.v1.trade.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "PM_MPS_USE_MNY", schema = "MPS")
@IdClass(UseMnyPK.class)
public class UseMny {


    @Id
    @Column(name = "LDGR_NO", nullable = false)
    private String ldgrNo;
    @Id
    @Column(name = "MAKE_DT", nullable = false)
    private String makeDt;
    @Id
    @Column(name = "USE_DATE", nullable = false)
    private LocalDateTime useDate;

    @Column(name = "TRD_DIV_CD", nullable = false)
    private String trdDivCd;

    @Column(name = "AMT_SIGN", nullable = false)
    private long amtSign;

    @Column(name = "USE_AMT", nullable = false)
    private long useAmt;

    @Column(name = "USE_TRD_NO", nullable = false)
    private String useTrdNo;

    @Column(name = "USE_TRD_DT")
    private String useTrdDt;

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
