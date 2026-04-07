package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotiCustDto {
    @JsonProperty("custNo")
    private String custNo;
    @JsonProperty("mCustId")
    private String mCustId;
    @JsonProperty("bizRegNo")
    private String bizRegNo;
    @JsonProperty("custStatCd")
    private String custStatCd;
    @JsonProperty("regDate")
    private String regDate;
    @JsonProperty("leaveDate")
    private String leaveDate;
    @JsonProperty("pktHash")
    private String pktHash;
}
