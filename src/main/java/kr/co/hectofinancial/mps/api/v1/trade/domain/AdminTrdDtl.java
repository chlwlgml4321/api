package kr.co.hectofinancial.mps.api.v1.trade.domain;

import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.AdminRsltStatCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "TB_MPS_ADMIN_TRD_DTL", schema = "MPS")
@IdClass(AdminTrdDtlPK.class)
public class AdminTrdDtl {

    @Id
    @Column(name = "SEQ_NO", nullable = false)
    private String seqNo;
    @Id
    @Column(name = "TRD_REQ_NO", nullable = false)
    private String trdReqNo;
    @Column(name = "MPS_CUST_NO", nullable = false)
    private String mpsCustNo;
    @Column(name = "RSLT_STAT_CD", nullable = false)
    private String rsltStatCd;
    @Column(name = "CUST_NM_MSK", nullable = false)
    private String custNmMsk;
    @Column(name = "CUST_NM_ENC", nullable = false)
    private String custNmEnc;
    @Column(name = "CPHONE_NO_ENC", nullable = false)
    private String cphoneNoEnc;
    @Column(name = "TRD_NO", nullable = false)
    private String trdNo;
    @Column(name = "TRD_DT", nullable = false)
    private String trdDt;
    @Column(name = "TRD_TM", nullable = false)
    private String trdTm;
    @Column(name = "TRD_AMT", nullable = false)
    private long trdAmt;
    @Column(name = "PROC_BF_BLC", nullable = false)
    private Long procBfBlc;
    @Column(name = "PROC_AFT_BLC", nullable = false)
    private Long procAftBlc;
    @Column(name = "RSLT_CD", nullable = false)
    private String rsltCd;
    @Column(name = "RSLT_MSG", nullable = false)
    private String rsltMsg;
    @Column(name = "RMK", nullable = false)
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
    public AdminTrdDtl(String seqNo, String trdReqNo, String mpsCustNo, String rsltStatCd, String custNmMsk, String custNmEnc,
                       String cphoneNoEnc, String trdNo, String trdDt, String trdTm, long trdAmt,
                       long procAftBlc, long procBfBlc, String rsltCd, String rsltMsg,
                       String rmk, LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp) {
        this.seqNo= seqNo;
        this.trdReqNo = trdReqNo;
        this.mpsCustNo = mpsCustNo;
        this.rsltStatCd = rsltStatCd;
        this.custNmMsk = custNmMsk;
        this.custNmEnc = custNmEnc;
        this.cphoneNoEnc =cphoneNoEnc;
        this.trdNo = trdNo;
        this.trdDt = trdDt;
        this.trdTm = trdTm;
        this.trdAmt=  trdAmt;
        this.procAftBlc = procAftBlc;
        this.procBfBlc = procBfBlc;
        this.rsltCd = rsltCd;
        this.rsltMsg=rsltMsg;
        this.rmk =rmk;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }

    public void hasNoCustNo(){
        this.rsltStatCd = AdminRsltStatCd.FAIL.getRsltStatCd();
        this.rsltCd = ErrorCode.CUSTOMER_NOT_FOUND.getErrorCode();
        this.rsltMsg = ErrorCode.CUSTOMER_NOT_FOUND.getErrorMessage();
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }

    public void hasCustNo(String custNo){
        this.rmk = custNo;
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }
    public void saveBfBlc(long blc){
        this.procBfBlc = blc;
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }

    public void saveAfBlc(long blc){
        this.procAftBlc = blc;
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }
    public void saveResultCode(String rsltStatCd, String rsltCd, String rsltMsg){
        this.rsltStatCd = rsltStatCd;
        this.rsltCd = rsltCd;
        this.rsltMsg = rsltMsg;
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }
    public void saveTrdNo(String trdNo, String trdDt, String trdTm){
        this.trdNo = trdNo;
        this.trdDt = trdDt;
        this.trdTm = trdTm;
        this.modifiedDate = LocalDateTime.now();
        this.modifiedId = ServerInfoConfig.HOST_NAME;
        this.modifiedIp = ServerInfoConfig.HOST_IP;
    }
}
