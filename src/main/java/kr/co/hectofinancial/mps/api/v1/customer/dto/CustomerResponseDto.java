package kr.co.hectofinancial.mps.api.v1.customer.dto;

import kr.co.hectofinancial.mps.api.v1.customer.domain.Customer;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.constant.CustDivCd;
import kr.co.hectofinancial.mps.global.constant.CustStatCd;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class CustomerResponseDto {
    private String custNo;
    @EncField
    private String custId;
    private String mid;
    private String custDivCd;
    private String custStatCd;
    private String regDt;
    private String regTm;
    private String csrcRegNoDivCd;
    @EncField
    private String csrcRegNo;
    private String kycKindCd;//1 CDD, 2 EDD
    private String kycExprDt;//Kyc 도래 일자
    //CDD 3년 EDD 1년
    private String bizDivCd;

    private Long hldLmtAmt; // 보유 한도 금액

    public static CustomerResponseDto of(Customer customer) {
        String kycExprDt = "";
        //기명이면서 회원상태 해지 아닌경우 kyc 분기처리 후 kyc 만료일자 계산
        if (CustDivCd.NAMED.getCustDivCd().equals(customer.getCustDivCd()) && !CustStatCd.WITHDRAW.getStatCd().equals(customer.getStatCd())) {
            if (StringUtils.isNotBlank(customer.getKycExecDt())) {
                kycExprDt = DateTimeUtil.getKycExpiredDate(customer.getKycKindCd(), customer.getKycExecDt());
            }
        }
        //회원상태 서비스해지 일 경우, mCustId substring
        String mCustId = customer.getMCustId();
        if (CustStatCd.WITHDRAW.getStatCd().equals(customer.getStatCd())) {
//            mCustId = mCustId.substring(mCustId.indexOf("[]") + 2, mCustId.lastIndexOf("[]"));
            mCustId = "";
        }
        return CustomerResponseDto.builder()
                .custNo(customer.getMpsCustNo())
                .custId(mCustId)
                .mid(customer.getMid())
                .custDivCd(customer.getCustDivCd())
                .custStatCd(customer.getStatCd())
                .regDt(customer.getCreatedDate().toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")).replaceAll("-", ""))
                .regTm(customer.getCreatedDate().toLocalTime().format(DateTimeFormatter.ofPattern("HHmmss")).replaceAll(":", ""))
                .csrcRegNoDivCd(StringUtils.isBlank(customer.getCsrcRegNoDivCd()) ? "" : customer.getCsrcRegNoDivCd())
                .csrcRegNo(StringUtils.isBlank(customer.getCsrcRegNoEnc()) ? "" : customer.getCsrcRegNoEnc())
                .kycKindCd(StringUtils.isBlank(customer.getKycKindCd()) ? "" : customer.getKycKindCd())
                .kycExprDt(kycExprDt)
                .bizDivCd(customer.getBizDivCd())
                .hldLmtAmt(customer.getChrgLmtAmlt())
                .build();
    }

    public static CustomerResponseDto of(CustomerDto customerDto) {
        String kycExprDt = "";
        //기명이면서 회원상태 해지 아닌경우 kyc 분기처리 후 kyc 만료일자 계산
        if (CustDivCd.NAMED.getCustDivCd().equals(customerDto.getCustDivCd()) && !CustStatCd.WITHDRAW.getStatCd().equals(customerDto.getStatCd())) {
            if (StringUtils.isNotBlank(customerDto.getKycExecDt())) {
                kycExprDt = DateTimeUtil.getKycExpiredDate(customerDto.getKycKindCd(), customerDto.getKycExecDt());
            }
        }
        //회원상태 서비스해지 일 경우, mCustId substring
        String mCustId = customerDto.getMCustId();
        if (CustStatCd.WITHDRAW.getStatCd().equals(customerDto.getStatCd())) {
//            mCustId = mCustId.substring(mCustId.indexOf("[]") + 2, mCustId.lastIndexOf("[]"));
            mCustId = "";
        }
        return CustomerResponseDto.builder()
                .custNo(customerDto.getMpsCustNo())
                .custId(mCustId)
                .mid(customerDto.getMid())
                .custDivCd(customerDto.getCustDivCd())
                .custStatCd(customerDto.getStatCd())
                .regDt(customerDto.getCreatedDate().toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")).replaceAll("-", ""))
                .regTm(customerDto.getCreatedDate().toLocalTime().format(DateTimeFormatter.ofPattern("HHmmss")).replaceAll(":", ""))
                .csrcRegNoDivCd(StringUtils.isBlank(customerDto.getCsrcRegNoDivCd()) ? "" : customerDto.getCsrcRegNoDivCd())
                .csrcRegNo(StringUtils.isBlank(customerDto.getCsrcRegNoEnc()) ? "" : customerDto.getCsrcRegNoEnc())
                .kycKindCd(StringUtils.isBlank(customerDto.getKycKindCd()) ? "" : customerDto.getKycKindCd())
                .kycExprDt(kycExprDt)
                .bizDivCd(customerDto.getBizDivCd())
                .hldLmtAmt(customerDto.getChrgLmtAmlt())
                .build();
    }

}
