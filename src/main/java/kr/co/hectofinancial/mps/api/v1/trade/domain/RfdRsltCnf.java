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
@Table(name = "TB_MPS_RFD_RSLT_CNF", schema = "MPS")
@IdClass(TradePK.class)
@Slf4j
public class RfdRsltCnf {

    @Id
    @Column(name = "TRD_NO")
    private String trdNo;
    @Id
    @Column(name = "TRD_DT")
    private String trdDt;
    @Column(name = "TRD_TM")
    private String trdTm;
    @Column(name = "RFD_TRD_NO")
    private String rfdTrdNo;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "MPS_CUST_NO")
    private String mpsCustNo;
    @Column(name = "TRD_AMT")
    private long trdAmt;
    @Column(name = "RSLT_CNF_STAT_CD")
    private String rsltCnfStatCd;
    @Column(name = "LAST_RSLT_CNF_DATE")
    private LocalDateTime lastRsltCnfDate;
    @Column(name = "RSLT_CNF_CNT")
    private long rsltCnfCnt;
    @Column(name = "REPROC_RFD_TRD_NO")
    private String reprocRfdTrdNo;
    @Column(name = "REPROC_RFD_DATE")
    private LocalDateTime reprocRfdDate;
    @Column(name = "REPROC_RFD_STAT_CD")
    private String reprocRfdStatCd;
    @Column(name = "REPROC_RFD_RSLT_CD")
    private String reprocRfdRsltCd;
    @Column(name = "RMK")
    private String rmk;
    @Column(name = "RFD_ACNT_BANK_CD")
    private String rfdAcntBankCd;
    @Column(name = "RFD_ACNT_NO_MSK")
    private String rfdAcntNoMsk;
    @Column(name = "RFD_ACNT_NO_ENC")
    private String rfdAcntNoEnc;
    @Column(name = "RMT_DIV_CD")
    private String rmtDivCd;
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
    public RfdRsltCnf(String trdNo, String trdDt, String trdTm, String mid, String mpsCustNo, String rfdTrdNo, long trdAmt, String rsltCnfStatCd, String rfdAcntBankCd, String rfdAcntNoMsk, String rfdAcntNoEnc, LocalDateTime lastRsltCnfDate, long rsltCnfCnt, String reprocRfdTrdNo, String reprocRfdStatCd,String reprocRfdRsltCd, LocalDateTime reprocRfdDate, String rmk, String rmtDivCd, LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp) {
        this.trdNo = trdNo;
        this.trdDt = trdDt;
        this.trdTm = trdTm;
        this.mid = mid;
        this.rfdTrdNo = rfdTrdNo;
        this.trdAmt = trdAmt;
        this.mpsCustNo = mpsCustNo;
        this.rsltCnfStatCd = rsltCnfStatCd;
        this.lastRsltCnfDate = lastRsltCnfDate;
        this.rsltCnfCnt = rsltCnfCnt;
        this.reprocRfdTrdNo = reprocRfdTrdNo;
        this.reprocRfdStatCd = reprocRfdStatCd;
        this.reprocRfdRsltCd = reprocRfdRsltCd;
        this.reprocRfdDate = reprocRfdDate;
        this.rmk = rmk;
        this.rfdAcntBankCd = rfdAcntBankCd;
        this.rfdAcntNoMsk = rfdAcntNoMsk;
        this.rfdAcntNoEnc = rfdAcntNoEnc;
        this.rmtDivCd = rmtDivCd;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }

    public void saveRetryTrade(String reprocRfdStatCd, String reprocRfdRsltCd, String reprocRfdTrdNo){
        this.reprocRfdStatCd = reprocRfdStatCd;
        this.reprocRfdRsltCd = reprocRfdRsltCd;
        this.reprocRfdTrdNo = reprocRfdTrdNo;
        this.reprocRfdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }
}
