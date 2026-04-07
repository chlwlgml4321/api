package kr.co.hectofinancial.mps.global.extern.whitelabel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PinCheckRequestDto {
    @JsonProperty("mId")
    private String mId;

    private String mreqNo;

    private String reqNo;

    private String custId;

    private String typeCd;

    private String pmtPwdEnc;

    @Override
    public String toString() {
        return "PinCheckRequestDto{" +
                "mId='" + mId + '\'' +
                ", reqNo='" + reqNo + '\'' +
                ", custId='" + custId + '\'' +
                '}';
    }
}
