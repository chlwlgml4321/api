package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotiAutoChargeRequsetDto {

    //필수값 o
    private String rsltCd;
    private String rsltMsg;
    private String mid;
    private String custNo;
    private String mCustId;
    private String trdDt;
    private String chargeType;
    private String mnyBlc;
    private String bankCd;
    private String bankInfo; //계좌번호 뒤 3자리
    private String tradeDate; //선불원장 성공거래일시, 선불원장 실패거래일시
    private String encKey;
    private String pktHash;

    //필수값 x
    private String trdNo;
    private String mnyAmt;
    private String chrgTrdNo; //pg거래번호
    private String chrgTradeDate; //pg거래일시
}
