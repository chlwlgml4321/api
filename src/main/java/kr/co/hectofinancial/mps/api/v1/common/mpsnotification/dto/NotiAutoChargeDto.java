package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotiAutoChargeDto {

    @JsonProperty("rsltCd")
    private String rsltCd;
    @JsonProperty("rsltMsg")
    private String rsltMsg;
    @JsonProperty("custNo")
    private String custNo;
    @JsonProperty("mCustId")
    private String mCustId;
    @JsonProperty("trdNo")
    private String trdNo;
    @JsonProperty("chargeType")
    private String chargeType;
    @JsonProperty("tradeDate")
    private String tradeDate;
    @JsonProperty("mnyAmt")
    private String mnyAmt;
    @JsonProperty("mnyBlc")
    private String mnyBlc;
    @JsonProperty("chrgTrdNo")
    private String chrgTrdNo;
    @JsonProperty("chrgTradeDate")
    private String chrgTradeDate;
    @JsonProperty("bankCd")
    private String bankCd;
    @JsonProperty("bankInfo")
    private String bankInfo;
    @JsonProperty("pktHash")
    private String pktHash;
}
