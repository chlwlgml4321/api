package kr.co.hectofinancial.mps.api.v1.notification.domain;

import kr.co.hectofinancial.mps.api.v1.deposit.domain.DpmnRcvPK;
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
@Table(name = "PY_NTC_SEND", schema = "TRD")
@IdClass(PyNtcSendPK.class)
public class PyNtcSend {

    @Id
    @Column(name = "NTC_SEND_NO")
    private String ntcSendNo;
    @Id
    @Column(name = "REG_DT")
    private String regDt;
    @Column(name = "SEND_REQ_DTM")
    private String sendReqDtm;
    @Column(name = "REQ_ALM_TYPE_CD")
    private String reqAlmTypeCd;
    @Column(name = "SEND_ALM_TYPE_CD")
    private String sendAlmTypeCd;
    @Column(name = "SEND_IDNT")
    private String sendIdnt;
    @Column(name = "RCV_IDNT_DIV_CD")
    private String rcvIdntDivCd;
    @Column(name = "RCV_IDNT")
    private String rcvIdnt;
    @Column(name = "PROC_STAT_CD")
    private String procStatCd;
    @Column(name = "CC")
    private String cc;
    @Column(name = "BCC")
    private String bcc;
    @Column(name = "TITLE")
    private String title;
    @Lob
    @Column(name = "CNTS")
    private String cnts;
    @Column(name = "TRY_CNT")
    private int tryCnt;
    @Column(name = "MSG_TMPL_ID")
    private String msgTmplId;
    @Column(name = "MSG_TMPL_PRMTR")
    private String msgTmplPrmtr;
    @Column(name = "BTN_PRMTR_1")
    private String btnPtmtr1;
    @Column(name = "BTN_PRMTR_2")
    private String btnPtmtr2;
    @Column(name = "BTN_PRMTR_3")
    private String btnPtmtr3;
    @Column(name = "BTN_PRMTR_4")
    private String btnPtmtr4;
    @Column(name = "BTN_PRMTR_5")
    private String btnPtmtr5;
    @Column(name = "LINK_TEXT")
    private String linkText;
    @Column(name = "LINK_URL")
    private String linkUrl;
    @Column(name = "RESRV_SEND_DTM")
    private String resrvSendDtm;
    @Column(name = "REF_TABLE_NM")
    private String refTableNm;
    @Column(name = "REF_TRD_NO")
    private String refTrdNo;
    @Column(name = "REF_TRD_DT")
    private String refTrdDt;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "ORN_ID")
    private String ornId;
    @Column(name = "ORN_SVC_KEY")
    private String ornSvcKey;
    @Column(name = "ORN_RSLT_CD")
    private String ornrsltCd;
    @Column(name = "NGW_REG_DTM")
    private String ngwRegDtm;
    @Column(name = "NGW_PROC_DTM")
    private String ngwProcDtm;
    @Column(name = "NGW_TRD_NO")
    private String ngwTrdNo;
    @Column(name = "NGW_STAT_CD")
    private String ngwStatCd;
    @Column(name = "NGW_RES_CD")
    private String ngwResCd;
    @Column(name = "NGW_ORN_STAT_CD")
    private String ngwOrnStatCd;
    @Column(name = "SEND_GRP_NO")
    private String sendGrpNo;
    @Column(name = "KEYMAN_ID")
    private String keymanId;
    @Column(name = "RPL_ALM_SEND_DIV_CD")
    private String rplAlmSendDivCd;
    @Column(name = "RMK")
    private String rmk;

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
    public PyNtcSend(String ntcSendNo, String regDt, String sendReqDtm, String reqAlmTypeCd, String sendIdnt, String rcvIdntDivCd, String rcvIdnt, String procStatCd, String cc, String bcc, String title, String cnts, int tryCnt, String msgTmplId, String msgTmplPrmtr, String btnPtmtr1, String btnPtmtr2, String btnPtmtr3, String btnPtmtr4, String btnPtmtr5,
                     String linkText, String linkUrl, String resrvSendDtm, String refTableNm, String refTrdNo, String refTrdDt, String mid, String ornId, String ornSvcKey, String ornrsltCd,
                     String ngwRegDtm, String ngwProcDtm, String ngwTrdNo, String ngwStatCd, String ngwResCd, String ngwOrnStatCd, String sendGrpNo, String keymanId, String rplAlmSendDivCd,
                     String rmk, LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp) {

        this.ntcSendNo = ntcSendNo;
        this.regDt = regDt;
        this.sendReqDtm = sendReqDtm;
        this.reqAlmTypeCd = reqAlmTypeCd;
        this.sendIdnt = sendIdnt;
        this.rcvIdntDivCd = rcvIdntDivCd;
        this.rcvIdnt = rcvIdnt;
        this.procStatCd = procStatCd;
        this.cc = cc;
        this.bcc = bcc;
        this.title = title;
        this.cnts = cnts;
        this.tryCnt = tryCnt;
        this.msgTmplId = msgTmplId;
        this.msgTmplPrmtr = msgTmplPrmtr;
        this.btnPtmtr1 = btnPtmtr1;
        this.btnPtmtr2 = btnPtmtr2;
        this.btnPtmtr3 = btnPtmtr3;
        this.btnPtmtr4 = btnPtmtr4;
        this.btnPtmtr5 = btnPtmtr5;
        this.linkText = linkText;
        this.linkUrl = linkUrl;
        this.resrvSendDtm = resrvSendDtm;
        this.refTableNm = refTableNm;
        this.refTrdNo = refTrdNo;
        this.refTrdDt = refTrdDt;
        this.mid = mid;
        this.ornId = ornId;
        this.ornSvcKey = ornSvcKey;
        this.ornrsltCd = ornrsltCd;
        this.ngwRegDtm = ngwRegDtm;
        this.ngwProcDtm = ngwProcDtm;
        this.ngwTrdNo = ngwTrdNo;
        this.ngwStatCd = ngwStatCd;
        this.ngwResCd = ngwResCd;
        this.ngwOrnStatCd = ngwOrnStatCd;
        this.sendGrpNo = sendGrpNo;
        this.keymanId = keymanId;
        this.rplAlmSendDivCd = rplAlmSendDivCd;
        this.rmk = rmk;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }
}
