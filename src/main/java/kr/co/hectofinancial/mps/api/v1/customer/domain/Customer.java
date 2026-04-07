package kr.co.hectofinancial.mps.api.v1.customer.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import kr.co.hectofinancial.mps.global.constant.CustStatCd;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "TB_MPS_CUST", schema = "MPS")
public class Customer extends BaseEntity {

    @Id
    @Column(name = "MPS_CUST_NO", nullable = false, updatable = false)
    private String mpsCustNo;

    @Column(name = "M_CUST_ID", nullable = false, updatable = false)
    @Convert(converter = DatabaseAESCryptoUtil.class)
    private String mCustId;

    @Column(name = "M_ID", nullable = false, updatable = false)
    private String mid;

    @Column(name = "CUST_NM", nullable = false, updatable = false)
    @Convert(converter = DatabaseAESCryptoUtil.class)
    private String custNm;

    @Column(name = "CUST_NM_MSK", nullable = false, updatable = false)
    private String custNmMsk;

    @Column(name = "CUST_NM_ENG", updatable = false)
//    @Convert(converter = DatabaseAESCryptoUtil.class)
    private String custNmEng;

    @Column(name = "CI_ENC", nullable = false, updatable = false)
    @Convert(converter = DatabaseAESCryptoUtil.class)
    private String ciEnc;

    @Column(name = "STAT_CD", nullable = false)
    private String statCd;

    @Column(name = "CPHONE_NO_MSK", nullable = false, updatable = false)
    private String cPhoneNoMsk;

    @Column(name = "CPHONE_NO_ENC", nullable = false, updatable = false)
    @Convert(converter = DatabaseAESCryptoUtil.class)
    private String cphoneNoEnc;

    @Column(name = "BIRTH_DT", updatable = false)
    private String birthDt;

    @Column(name = "NTN_CD", updatable = false)
    private String ntnCd;

    @Column(name = "LIVE_NTN_CD", updatable = false)
    private String liveNtnCd;

    @Column(name = "GNDR_CD", updatable = false)
    private String gndrCd;

//    private String gndrNm;//			, BAS.PKG_CM_FUNC.FN_CD_NM('MPS_GNDR_CD', GNDR_CD) AS GNDR_NM

    @Column(name = "HOME_ZIP_CD", updatable = false)
    private String homeZipCd;

    @Column(name = "HOME_ADDR", updatable = false)
    private String homeAddr;

    @Column(name = "HOME_DTL_ADDR", updatable = false)
    private String homeDtlAddr;

    @Column(name = "WORK_DTL_CD", updatable = false)
    private String workDtlCd;

    @Column(name = "WORK_NM", updatable = false)
    private String workNm;

    @Column(name = "WORK_ZIP_CD", updatable = false)
    private String workZipCd;

    @Column(name = "WORK_ADDR", updatable = false)
    private String workAddr;

    @Column(name = "WORK_DTL_ADDR", updatable = false)
    private String wrokDtlAddr;

    @Column(name = "FUND_SRC_DIV_CD", updatable = false)
    private String fundSrcDivCd;

    @Column(name = "FUND_SRC_NM", updatable = false)
    private String fundSrcNm;

    @Column(name = "FUND_SRC_ETC", updatable = false)
    private String funSrcEtc;

    @Column(name = "TRD_PURPS_DIV_CD", updatable = false)
    private String trdPurpsDivCd;

    @Column(name = "TRD_PURPS_NM", updatable = false)
    private String trdPurpsNm;

    @Column(name = "TRD_PURPS_ETC", updatable = false)
    private String trdPurpsEtc;

    @Column(name = "EMAIL", updatable = false)
    private String email;

    @Column(name = "IDNTY_CNF_RSLT", updatable = false)
    private String idntyCnfRslt;

    @Column(name = "KYC_KIND_CD", updatable = false)
    private String kycKindCd;

    @Column(name = "KYC_EXEC_DT", updatable = false)
    private String kycExecDt;

    @Column(name = "CUST_DIV_CD", nullable = false, updatable = false)
    private String custDivCd;

    @Column(name = "CHRG_LMT_AMT", nullable = false, updatable = false)
    private Long chrgLmtAmlt;

    @Column(name = "CSRC_REG_NO_DIV_CD", updatable = false)
    private String csrcRegNoDivCd;

    @Column(name = "CSRC_REG_NO_ENC", updatable = false)
//    @Convert(converter = DatabaseAESCryptoUtil.class)
    private String csrcRegNoEnc;

    @Column(name = "RMK", updatable = false)
    private String rmk;

    @Column(name = "BIZ_DIV_CD", updatable = false)
    private String bizDivCd;
    @Column(name = "BIZ_REG_NO", updatable = false)
    private String bizRegNo;
    @Column(name = "BILL_KEY_ENC", updatable = false)
    private String billKeyEnc;

    public void lockCustomerStatCd() {
        this.statCd = CustStatCd.LOCK.getStatCd();
    }
}
