package kr.co.hectofinancial.mps.api.v1.deposit.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_MPS_DPMN_RCV_DTL", schema = "MPS")
@IdClass(DpmnRcvDtlPK.class)
public class DpmnRcvDtl {

    @Id
    @Column(name = "TRD_DT")
    private String trdDt;
    @Id
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "DP_NOTI_NO")
    private String dpNotiNo;
    @Column(name = "DP_DT")
    private String dpDt;
    @Column(name = "DP_AMT")
    private long dpAmt;
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

    @Column(updatable = true, name = "UPDT_ID")
    private String modifiedId;

    @Column(updatable = true, name = "UPDT_IP")
    private String modifiedIp;

    @Builder
    public DpmnRcvDtl(String trdDt, String mid, String dpNotiNo, String dpDt, long dpAmt, String rmk, LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp){
        this.trdDt = trdDt;
        this.mid = mid;
        this.dpAmt = dpAmt;
        this.dpNotiNo = dpNotiNo;
        this.dpDt = dpDt;
        this.rmk = rmk;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }
}
