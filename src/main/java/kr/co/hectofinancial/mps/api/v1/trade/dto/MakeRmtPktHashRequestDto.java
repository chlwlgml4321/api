package kr.co.hectofinancial.mps.api.v1.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MakeRmtPktHashRequestDto {
    public String mchtId; //상점아이디
    public String mchtTrdNo; //상점주문번호(한글제외)
    public String trdDt;
    public String trdTm;
    public String bankCd;
    public String custAcntNo; //계좌번호 평문
    public String trdAmt; //aes암호화?
    public String priKey;

}
