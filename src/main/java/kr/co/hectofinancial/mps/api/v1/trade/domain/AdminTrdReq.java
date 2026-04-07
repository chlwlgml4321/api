package kr.co.hectofinancial.mps.api.v1.trade.domain;

import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "TB_MPS_ADMIN_TRD_REQ", schema = "MPS")
@Slf4j
public class AdminTrdReq {

    @Id
    @Column(name = "TRD_REQ_NO")
    private String trdReqNo;
    @Column(name = "TRD_REQ_NM")
    private String trdReqNm;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "TRD_DIV_CD")
    private String trdDivCd;
    @Column(name = "TRD_DIV_DTL_CD")
    private String trdDivDtlCd;
    @Column(name = "CHRG_MEAN_CD")
    private String chrgMeanCd;
    @Column(name = "VLD_PD")
    private String vldPd;
    @Column(name = "REQ_CNT")
    private long reqCnt;
    @Column(name = "REQ_AMT")
    private long reqAmt;
    @Column(name = "PROC_STAT_CD")
    private String procStatCd;
    @Column(name = "SUCC_CNT")
    private Long succCnt;
    @Column(name = "SUCC_AMT")
    private Long succAmt;
    @Column(name = "FAIL_CNT")
    private Long failCnt;
    @Column(name = "FAIL_AMT")
    private Long failAmt;
    @Column(name = "PROC_ST_DATE")
    private LocalDateTime procStDate;
    @Column(name = "PROC_ED_DATE")
    private LocalDateTime procEdDate;
    @Column(name = "M_RESRV_FIELD_1")
    private String mResrvField1;
    @Column(name = "RMK")
    private String rmk;
    @CreatedDate
    @Column(updatable = false, name = "INST_DATE")
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime createdDate;
    @Column(updatable = false, name = "INST_ID")
    private String createdId;
    @Column(updatable = false, name = "INST_IP")
    private String createdIp;
    @LastModifiedDate
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    @Column(insertable = false, name = "UPDT_DATE")
    private LocalDateTime modifiedDate;
    @Column(updatable = true, name = "UPDT_ID")
    private String modifiedId;
    @Column(updatable = true, name = "UPDT_IP")
    private String modifiedIp;

    @Builder
    public AdminTrdReq(String trdReqNo, String trdReqNm, String mid, String trdDivCd,  String trdDivDtlCd, String chrgMeanCd,
                       String vldPd, long reqCnt, long reqAmt,
                       String procStatCd, long succCnt, long succAmt, long failCnt, long failAmt, LocalDateTime procStDate, LocalDateTime procEdDate,
                       String mResrvField1, String rmk, LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp) {
        this.trdReqNo = trdReqNo;
        this.trdReqNm =trdReqNm;
        this.trdDivCd = trdDivCd;
        this.trdDivDtlCd = trdDivDtlCd;
        this.mid = mid;
        this.chrgMeanCd = chrgMeanCd;
        this.vldPd = vldPd;
        this.reqCnt = reqCnt;
        this.reqAmt = reqAmt;
        this.procStatCd = procStatCd;
        this.succCnt = succCnt;
        this.succAmt = succAmt;
        this.failAmt = failAmt;
        this.failCnt = failCnt;
        this.procStDate = procStDate;
        this.procEdDate = procEdDate;
        this.mResrvField1 = mResrvField1;
        this.rmk =rmk;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }

    public void saveProcStDate(){
        this.procStDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }

    public void saveResult(String procStatCd, long succCnt, long succAmt, long failCnt, long failAmt){
        this.procEdDate = LocalDateTime.now();
        this.procStatCd = procStatCd;
        this.succCnt = succCnt;
        this.failCnt = failCnt;
        this.succAmt = succAmt;
        this.failAmt = failAmt;
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }
}
