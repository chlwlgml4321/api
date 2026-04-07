package kr.co.hectofinancial.mps.api.v1.firm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoneyWithdrawRequestDto {

    @JsonProperty("hdInfo")
    private String hdInfo;
    @JsonProperty("mchtId")
    private String mchtId;
    @JsonProperty("mchtTrdNo")
    private String mchtTrdNo;
    @JsonProperty("mchtCustId")
    private String mchtCustId;
    @JsonProperty("trdDt")
    private String trdDt;
    @JsonProperty("trdTm")
    private String trdTm;
    @JsonProperty("bankCd")
    private String bankCd;
    @JsonProperty("custAcntNo")
    private String custAcntNo;
    @JsonProperty("custAcntSumry")
    private String custAcntSumry;
    @JsonProperty("trdAmt")
    private String trdAmt;
    @JsonProperty("macntSumry")
    private String macntSumry;
    @JsonProperty("pktHash")
    private String pktHash;
}
