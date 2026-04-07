package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotiCardDto {
    @JsonProperty("custNo")
    private String custNo;
    @JsonProperty("mCustId")
    private String mCustId;
    @JsonProperty("trdNo")
    private String trdNo;
    @JsonProperty("orgTrdNo")
    private String orgTrdNo;
    @JsonProperty("orgTrdDt")
    private String orgTrdDt;
    @JsonProperty("tradeType")
    private String tradeType;
    @JsonProperty("tradeDate")
    private String tradeDate;
    @JsonProperty("mnyAmt")
    private String mnyAmt;
    @JsonProperty("pntAmt")
    private String pntAmt;
    @JsonProperty("waitMnyAmt")
    private String waitMnyAmt;
    @JsonProperty("mnyBlc")
    private String mnyBlc;
    @JsonProperty("pntBlc")
    private String pntBlc;
    @JsonProperty("waitMnyBlc")
    private String waitMnyBlc;
    @JsonProperty("joinNo")
    private String joinNo;
    @JsonProperty("joinNm")
    private String joinNm;
    @JsonProperty("prdtNo")
    private String prdtNo;
    @JsonProperty("pktHash")
    private String pktHash;
}
