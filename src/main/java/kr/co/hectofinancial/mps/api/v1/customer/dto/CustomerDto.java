package kr.co.hectofinancial.mps.api.v1.customer.dto;

import kr.co.hectofinancial.mps.api.v1.customer.domain.Customer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class CustomerDto {
    private String mpsCustNo;
    private String mCustId;
    private String mid;
    private String custNm;
    private String custNmMsk;
    private String custNmEng;
    private String ciEnc;
    private String statCd;
    private String cPhoneNoMsk;
    private String cPhoneNoEnc;
    private String birthDt;
    private String ntnCd;
    private String liveNtnCd;
    private String gndrCd;
    private String gndrNm;
    private String homeZipCd;
    private String homeAddr;
    private String homeDtlAddr;
    private String workDtlCd;
    private String workNm;
    private String workZipCd;
    private String workAddr;
    private String wrokDtlAddr;
    private String fundSrcDivCd;
    private String fundSrcNm;
    private String funSrcEtc;
    private String trdPurpsDivCd;
    private String trdPurpsNm;
    private String trdPurpsEtc;
    private String email;
    private String idntyCnfRslt;
    private String kycKindCd;
    private String kycExecDt;
    private String custDivCd;
    private Long chrgLmtAmlt;
    private String csrcRegNoDivCd;
    private String csrcRegNoEnc;
    private String rmk;
    private String bizDivCd;
    private String bizRegNo;
    private String billKeyEnc;
    private LocalDateTime createdDate;
    private String createdId;
    private String createdIp;
    private LocalDateTime modifiedDate;
    private String modifiedId;
    private String modifiedIp;

    public static CustomerDto of(Customer customer) {
        return CustomerDto.builder()
                .mpsCustNo(customer.getMpsCustNo())
                .mCustId(customer.getMCustId())
                .mid(customer.getMid())
                .custNm(customer.getCustNm())
                .custNmMsk(customer.getCustNmMsk())
                .custNmEng(customer.getCustNmEng())
                .ciEnc(customer.getCiEnc())
                .statCd(customer.getStatCd())
                .cPhoneNoMsk(customer.getCPhoneNoMsk())
                .cPhoneNoEnc(customer.getCphoneNoEnc())
                .birthDt(customer.getBirthDt())
                .ntnCd(customer.getNtnCd())
                .liveNtnCd(customer.getLiveNtnCd())
                .gndrCd(customer.getGndrCd())
                .homeZipCd(customer.getHomeZipCd())
                .homeAddr(customer.getHomeAddr())
                .homeDtlAddr(customer.getHomeDtlAddr())
                .workDtlCd(customer.getWorkDtlCd())
                .workNm(customer.getWorkNm())
                .workZipCd(customer.getWorkZipCd())
                .workAddr(customer.getWorkAddr())
                .wrokDtlAddr(customer.getWrokDtlAddr())
                .fundSrcDivCd(customer.getFundSrcDivCd())
                .fundSrcNm(customer.getFundSrcNm())
                .funSrcEtc(customer.getFunSrcEtc())
                .trdPurpsDivCd(customer.getTrdPurpsDivCd())
                .trdPurpsNm(customer.getTrdPurpsNm())
                .trdPurpsEtc(customer.getTrdPurpsEtc())
                .email(customer.getEmail())
                .idntyCnfRslt(customer.getIdntyCnfRslt())
                .kycKindCd(customer.getKycKindCd())
                .kycExecDt(customer.getKycExecDt())
                .custDivCd(customer.getCustDivCd())
                .chrgLmtAmlt(customer.getChrgLmtAmlt())
                .csrcRegNoDivCd(customer.getCsrcRegNoDivCd())
                .csrcRegNoEnc(customer.getCsrcRegNoEnc())
                .rmk(customer.getRmk())
                .bizDivCd(customer.getBizDivCd())
                .bizRegNo(customer.getBizRegNo())
                .billKeyEnc(customer.getBillKeyEnc())
                .createdDate(customer.getCreatedDate())
                .createdId(customer.getCreatedId())
                .createdIp(customer.getCreatedIp())
                .modifiedDate(customer.getModifiedDate())
                .modifiedId(customer.getModifiedId())
                .modifiedIp(customer.getModifiedIp())
                .build();
    }

    @Override
    public String toString() {
        return "CustomerDto{" +
                "mpsCustNo='" + mpsCustNo + '\'' +
                ", custNmMsk='" + custNmMsk + '\'' +
                ", statCd='" + statCd + '\'' +
                ", birthDt='" + birthDt + '\'' +
                ", ntnCd='" + ntnCd + '\'' +
                ", liveNtnCd='" + liveNtnCd + '\'' +
                ", gndrCd='" + gndrCd + '\'' +
                ", custDivCd='" + custDivCd + '\'' +
                ", chrgLmtAmlt=" + chrgLmtAmlt +
                ", kycExecDt=" + kycExecDt +
                ", createdDate=" + createdDate +
                ", createdId='" + createdId + '\'' +
                ", createdIp='" + createdIp + '\'' +
                '}';
    }
}
