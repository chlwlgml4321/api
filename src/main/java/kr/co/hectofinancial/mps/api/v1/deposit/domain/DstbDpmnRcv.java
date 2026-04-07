package kr.co.hectofinancial.mps.api.v1.deposit.domain;

import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
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
@Table(name = "TB_DSTB_DPMN_RCV", schema = "MPS")
@IdClass(DstbDpmnRcvPK.class)
public class DstbDpmnRcv{

    @Id
    @Column(name = "TRD_DT")
    private String trdDt;
    @Id
    @Column(name = "M_ID")
    private String mid;

    @Column(name = "DP_REQ_AMT")
    private long dpReqAmt;

    @Column(name = "DP_AMT")
    private long dpAmt;

    @Column(name = "DP_EXCS_AMT")
    private long dpExcsAmt;

    @Column(name = "RMK")
    private String rmk;

    @Column(name = "DP_STAT_CD")
    private String dpStatCd;

    @Column(name = "ACM_DP_REQ_AMT")
    private long acmDpReqAmt;
    @Column(name = "ACM_DP_AMT")
    private long acmDpAmt;
    @Column(name = "ACM_DP_EXCS_AMT")
    private long acmDpExcsAmt;
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
    public DstbDpmnRcv(String trdDt, String mid, long dpReqAmt, long dpAmt, long dpExcsAmt, String rmk, String dpStatCd, long acmDpAmt, long acmDpReqAmt, long acmDpExcsAmt,
                       LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp){
        this.trdDt = trdDt;
        this.mid = mid;
        this.dpReqAmt = dpReqAmt;
        this.dpAmt = dpAmt;
        this.dpExcsAmt = dpExcsAmt;
        this.rmk = rmk;
        this.dpStatCd = dpStatCd;
        this.acmDpAmt = acmDpAmt;
        this.acmDpReqAmt = acmDpReqAmt;
        this.acmDpExcsAmt = acmDpExcsAmt;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }

    public void orgDpmnRcdUpdate() {
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }

}
