package kr.co.hectofinancial.mps.api.v1.csrc.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "PM_EZP_CSRC_ISS", schema = "TRD")
@IdClass(EzpCsrcIssPK.class)
public class EzpCsrcIss {

    @Id
    @Column(name = "ISS_REQ_NO", updatable = false)
    private String issReqNo;
    @Id
    @Column(name = "TRD_DT")
    private String trdDt;
    @Column(name = "TRD_TM")
    private String trdTm;
    @Column(name = "TRD_NO")
    private String trdNo;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "M_CUST_ID")
    private String mCustId;
    @Column(name = "REQ_DTM")
    private String reqDtm;
    @Column(name = "CNCL_YN")
    private String cnclYn;
    @Column(name = "TRD_AMT")
    private long trdAmt;
    @Column(name = "SPL_AMT")
    private long splAmt; //공급가
    @Column(name = "VAT")
    private long vat; //부가세
    @Column(name = "SVC_AMT")
    private long svcAmt; //봉사료
    @Column(name = "TAX_TYPE_CD")
    private String taxTypeCd; //과세 타입 코드
    @Column(name = "IN_STAT_CD")
    private String inStatCd; //내부 상태 코드
    @Column(name = "OUT_STAT_CD")
    private String outStatCd; //외부 상태 코드
    @Column(name = "ORN_TRD_NO")
    private String ornTrdNo; //원천사 거래 번호
    @Column(name = "ORN_TRD_DTM")
    private String ornTrdDtm; //원천사 거래일시
    @Column(name = "ORN_SVC_KEY")
    private String ornSvcKey; //원천사 서비스 키
    @Column(name = "ORN_RSLT_CD")
    private String ornRsltCd; //원천사 결과 코드
    @Column(name = "ORN_RSLT_MSG")
    private String ornRsltMsg; //원천사 결과 메세지
    @Column(name = "BIZ_REG_NO")
    private String bizRegNo;
//    @Column(name = "CPN_NM")
//    private String cpnNm;
//    @Column(name = "CPN_TEL_NO")
//    private String cpnTelNo;
//    @Column(name = "REP_NM")
//    private String repNm;
    @Column(name = "M_TRD_NO")
    private String mtrdNo; //상점 거래 번호
    @Column(name = "CUST_NM")
    private String custNm; //고객 명
    @Column(name = "ISS_PURPS_DIV_CD")
    private String issPurpsDivCd; //발급 목적 구분 코드
    @Column(name = "CSRC_REG_NO_ENC")
    private String csrcRegNoEnc; //현금영수증 등록번호 암호화
    @Column(name = "CSRC_REG_NO_DIV_CD")
    private String csrcRegNoDivCd; //현금영수증 등록번호 구분코드
    @Column(name = "ORN_ORG_TRD_NO")
    private String ornOrgTrdNo; //원천사 원거래 번호
    @Column(name = "ORN_ORG_TRD_DTM")
    private String ornOrgTrdDtm; //원천사 원거래 일시
    @Column(name = "ORG_ISS_REQ_NO")
    private String orgIssReqNo; //원 발급 요청 번호
    @Column(name = "REPROC_YN")
    private String reprocYn; //재처리 여부
    @Column(name = "NTS_ERR_CD")
    private String ntsErrCd; //국세청 에러 코드
    @Column(name = "NTS_RSLT_DTM")
    private String ntsRsltDtm; //국세청 결과 일시
    @Column(name = "SVC_CD")
    private String svcCd;
    @Column(name = "PRDT_CD")
    private String prdtCd;
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
    public EzpCsrcIss(String issReqNo, String trdNo, String trdDt, String trdTm, long trdAmt, long splAmt, long vat, long svcAmt, String bizRegNo, String reqDtm, String taxTypeCd, String inStatCd, String outStatCd, String ornTrdNo, String ornTrdDtm, String ornSvcKey, String ornRsltCd, String ornRsltMsg, String mCustId, String svcCd, String prdtCd, String mid, String cnclYn, String mtrdNo, String custNm, String issPurpsDivCd, String csrcRegNoEnc, String csrcRegNoDivCd, String ornOrgTrdNo, String ornOrgTrdDtm, String ntsErrCd, String ntsRsltDtm, String reprocYn, String orgIssReqNo, LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp) {
        this.issReqNo = issReqNo;
        this.trdNo = trdNo;
        this.trdDt = trdDt;
        this.trdTm = trdTm;
        this.svcCd = svcCd;
        this.prdtCd = prdtCd;
        this.mid = mid;
        this.trdAmt = trdAmt;
        this.splAmt = splAmt;
        this.vat = vat;
        this.svcAmt = svcAmt;
        this.reqDtm = reqDtm;
        this.mCustId = mCustId;
        this.inStatCd = inStatCd;
        this.outStatCd = outStatCd;
        this.taxTypeCd = taxTypeCd;
        this.ornTrdNo = ornTrdNo;
        this.ornTrdDtm = ornTrdDtm;
        this.ornSvcKey = ornSvcKey;
        this.mtrdNo = mtrdNo;
        this.custNm = custNm;
        this.bizRegNo = bizRegNo;
        this.ornRsltCd = ornRsltCd;
        this.ornRsltMsg = ornRsltMsg;
        this.issPurpsDivCd = issPurpsDivCd;
        this.csrcRegNoDivCd = csrcRegNoDivCd;
        this.csrcRegNoEnc = csrcRegNoEnc;
        this.ntsErrCd = ntsErrCd;
        this.ntsRsltDtm = ntsRsltDtm;
        this.ornOrgTrdNo = ornOrgTrdNo;
        this.ornOrgTrdDtm = ornOrgTrdDtm;
        this.reprocYn = reprocYn;
        this.orgIssReqNo = orgIssReqNo;
        this.cnclYn = cnclYn;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }

}
