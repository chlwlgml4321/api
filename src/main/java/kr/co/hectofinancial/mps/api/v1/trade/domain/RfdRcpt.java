package kr.co.hectofinancial.mps.api.v1.trade.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "TB_RFD_RCPT", schema = "TRD")
@Slf4j
public class RfdRcpt {
    @Id
    @Column(name = "RFD_RCPT_NO")
    private String rfdRcptNo;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "SVC_CD")
    private String svcCd;
    @Column(name = "PRDT_CD")
    private String prdtCd;
    @Column(name = "RCPT_DT")
    private String rcptDt;
    @Column(name = "RCPT_TM")
    private String rcptTm;
    @Column(name = "ORG_TRD_AMT")
    private long orgTrdAmt;
    @Column(name = "ORG_TRD_NO")
    private String orgTrdNo;
    @Column(name = "ORG_TRD_DT")
    private String orgTrdDt;
    @Column(name = "RFD_AMT")
    private long rfdAmt;
    @Column(name = "RFD_DT")
    private String rfdDt; //실제 환불일자
    @Column(name = "RFD_SCH_DT")
    private String rfdSchDt;
    @Column(name = "RFD_ACNT_BANK_CD")
    private String rfdAcntBankCd;
    @Column(name = "RFD_ACNT_NO_MSK")
    private String rfdAcntNoMsk;
    @Column(name = "RFD_ACNT_NO_ENC")
    private String rfdAcntNoEnc;
    @Column(name = "RFD_ACNT_DPR_NM")
    private String rfdAcntDprNm;
    @Column(name = "RFD_ACNT_SUMRY")
    private String rfdAcntSumry;
    @Column(name = "RFD_STAT_CD")
    private String rfdStatCd;
    @Column(name = "MACNT_BANK_CD")
    private String macntBankCd;
    @Column(name = "MACNT_NO_ENC")
    private String macntNoEnc;
    @Column(name = "MACNT_SUMRY")
    private String macntSumry;
    @Column(name = "MACNT_NO_MSK")
    private String macntNoMsk;
    @Column(name = "RETRY_CNT")
    private long retryCnt;
    @Column(name = "RFD_APPR_STAT_CD")
    private String rfdApprStatCd;
    @Column(name = "RMK")
    private String rmk;
    @Column(name = "TRSF_M_ID")
    private String trsfMid;

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
    public RfdRcpt(String rfdRcptNo, String svcCd, String prdtCd, String mid, String rcptDt, String rcptTm, long orgTrdAmt, String orgTrdNo, String orgTrdDt, long rfdAmt, String rfdDt, String rfdSchDt, String rfdAcntBankCd,
                  String rfdAcntNoMsk, String rfdAcntNoEnc, String rfdAcntDprNm, String rfdAcntSumry, String rfdStatCd, String macntBankCd, String macntNoEnc, String macntSumry, String trsfMid,
                   String macntNoMsk, long retryCnt, String rfdApprStatCd, String rmk, LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp) {
        this.rfdRcptNo = rfdRcptNo;
        this.svcCd = svcCd;
        this.prdtCd = prdtCd;
        this.mid = mid;
        this.rcptDt = rcptDt;
        this.rcptTm = rcptTm;
        this.orgTrdAmt = orgTrdAmt;
        this.orgTrdNo = orgTrdNo;
        this.orgTrdDt = orgTrdDt;
        this.rfdAmt = rfdAmt;
        this.rfdDt = rfdDt;
        this.rfdSchDt = rfdSchDt;
        this.rfdAcntBankCd = rfdAcntBankCd;
        this.rfdAcntNoMsk = rfdAcntNoMsk;
        this.rfdAcntNoEnc = rfdAcntNoEnc;
        this.rfdAcntDprNm = rfdAcntDprNm;
        this.rfdAcntSumry = rfdAcntSumry;
        this.rfdStatCd = rfdStatCd;
        this.retryCnt = retryCnt;
        this.macntNoEnc = macntNoEnc;
        this.macntSumry = macntSumry;
        this.macntNoMsk = macntNoMsk;
        this.macntBankCd = macntBankCd;
        this.rfdApprStatCd = rfdApprStatCd;
        this.rmk = rmk;
        this.trsfMid = trsfMid;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }

}
