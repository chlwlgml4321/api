package kr.co.hectofinancial.mps.api.v1.firm.domain;

import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "PM_PA_DPMN_NOTI", schema = "TRD")
@IdClass(DpmnNotiPK.class)
public class DpmnNoti {

    @Id
    @Column(name = "GLOBAL_ID")
    private String globalId;
    @Id
    @Column(name = "TRD_DT")
    private String trdDt;
    @Column(name = "SVC_CD")
    private String svcCd;
    @Column(name = "TRD_TM")
    private String trdTm;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "OUT_STAT_CD")
    private String outStatCd;
    @Column(name = "OUT_RSLT_GRP_CD")
    private String outRsltGrpCd;
    @Column(name = "OUT_RSLT_CD")
    private String outRsltCd;
    @Column(name = "CRC_CD")
    private String crcCd;
    @Column(name = "TRD_AMT")
    private long trdAmt;
    @Column(name = "ORN_ID")
    private String ornId;
    @Column(name = "ORN_PKT_DIV_CD")
    private String ornPktDivCd;
    @Column(name = "ORN_JOB_DIV_CD")
    private String ornJobDivCd;
    @Column(name = "ORN_PKT_NO")
    private String ornPktNo;
    @Column(name = "ORN_TRD_DT")
    private String ornTrdDt;
    @Column(name = "ORN_TRD_Tm")
    private String ornTrdTm;
    @Column(name = "ORN_SVC_KEY")
    private String ornSvckey;
    @Column(name = "ORN_RSLT_CD")
    private String ornRsltCd;
    @Column(name = "VAN_ISTT_CD")
    private String vanIsttCd;
    @Column(name = "NOTI_CLSS_CD")
    private String notiClssCd;
    @Column(name = "NOTI_TRD_DIV_CD")
    private String notiTrdDivCd;
    @Column(name = "GIRO_CD")
    private String giroCd;
    @Column(name = "RMK")
    private String rmk;
    @Column(name = "MACNT_BANK_CD")
    private String macntBankCd;
    @Column(name = "MACNT_NO_ENC")
    private String macntNoEnc;
    @Column(name = "MACNT_NO_MSK")
    private String macntNoMsk;
    @Column(name = "MACNT_SUMRY_1")
    private String macntSumry1;
    @CreatedDate
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    @Column(updatable = false, name = "INST_DATE")
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
    public DpmnNoti(String globalId, String trdDt, String trdTm, String mid, String svcCd, String outStatCd, String outRsltGrpCd, String outRsltCd, String crcCd, long trdAmt, String ornId, String ornPktDivCd, String ornJobDivCd, String ornPktNo,
                    String ornTrdDt, String ornTrdTm, String ornSvckey, String ornRsltCd, String vanIsttCd, String  notiClssCd, String notiTrdDivCd, String giroCd, String rmk, String macntNoEnc, String macntBankCd, String macntNoMsk, String macntSumry1, LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp){
        this.globalId = globalId;
        this.trdDt = trdDt;
        this.trdTm = trdTm;
        this.mid = mid;
        this.svcCd = svcCd;
        this.outStatCd = outStatCd;
        this.outRsltGrpCd = outRsltGrpCd;
        this.outRsltCd = outRsltCd;
        this.crcCd = crcCd;
        this.trdAmt = trdAmt;
        this.ornId = ornId;
        this.ornPktDivCd = ornPktDivCd;
        this.ornJobDivCd = ornJobDivCd;
        this.ornPktNo = ornPktNo;
        this.ornTrdDt = ornTrdDt;
        this.ornTrdTm = ornTrdTm;
        this.ornSvckey = ornSvckey;
        this.ornRsltCd = ornRsltCd;
        this.vanIsttCd = vanIsttCd;
        this.notiClssCd = notiClssCd;
        this.notiTrdDivCd = notiTrdDivCd;
        this.giroCd = giroCd;
        this.rmk = rmk;
        this.macntBankCd = macntBankCd;
        this.macntNoEnc = macntNoEnc;
        this.macntNoMsk = macntNoMsk;
        this.macntSumry1 = macntSumry1;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }

    public void dpmnNotiFailSave(){
        this.outRsltCd = "0001";
        this.outStatCd = "0031";
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }

    public void dpmnNotiRmkSave(String rmk) {
        this.rmk = rmk;
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }
}
