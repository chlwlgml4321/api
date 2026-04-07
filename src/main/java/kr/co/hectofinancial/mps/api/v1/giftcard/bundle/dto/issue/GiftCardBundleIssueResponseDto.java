package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import lombok.*;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GiftCardBundleIssueResponseDto {

    @HashField(order = 1)
    private String gcDstbNo;

    @HashField(order = 2)
    private String useMid; // 상점 아이디

    @HashField(order = 3)
    @JsonProperty("mTrdNo")
    private String mTrdNo;

    @HashField(order = 4)
    private String dstbTrdNo;

    @EncField
    @HashField(order = 5)
    private String gcBndlNo;

    @HashField(order = 6)
    private String trdAmt;

    @HashField(order = 7)
    private String dstbBlc; // 유통잔액

    private String issDt;
    private String issTm;
    private String vldDt;
    private String gcBndlStatCd;
    private String pktHash;
}
