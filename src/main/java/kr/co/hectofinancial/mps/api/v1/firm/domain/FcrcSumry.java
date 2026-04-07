package kr.co.hectofinancial.mps.api.v1.firm.domain;

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
@Table(name = "TB_M_PA_DPMN_SUMRY", schema = "BAS")
@IdClass(FcrcSumryPK.class)
public class FcrcSumry {

    @Id
    @Column(name = "ACNT_SUMRY")
    private String acntSumry;
    @Id
    @Column(name = "VLD_ED_DATE")
    private String vldEdDate;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "VLD_ST_DATE")
    private String vldStDate;
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
}
