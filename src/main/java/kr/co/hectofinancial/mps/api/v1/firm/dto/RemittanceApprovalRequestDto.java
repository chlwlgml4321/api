package kr.co.hectofinancial.mps.api.v1.firm.dto;

import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RemittanceApprovalRequestDto {
/**
 * 간편현금결제
 */

    public String hdInfo = "SPAY_AROW_1.0"; //전문정보 고정값
    public String mchtId; //상점아이디
    public String mchtTrdNo; //상점주문번호(한글제외)
    public String mchtCustId; //상점 uniq 고객키 aes암호화
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String trdDt;
    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String trdTm;
    public String bankCd;
    public String custAcntNo; //계좌번호 aes암호화
    public String custAcntSumry; //입금통장인자내용(7자리) aes암호화
    public Number trdAmt; //aes암호화
    public String macntSumry;
    public String pktHash; //sha256  상점아이디+주문번호+거래일자+거래시간+입금은행코드+입금계좌번호(평문)+거래금액+인증키
    
}
