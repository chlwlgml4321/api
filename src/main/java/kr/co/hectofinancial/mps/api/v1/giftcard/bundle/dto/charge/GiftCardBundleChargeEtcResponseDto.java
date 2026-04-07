package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import lombok.*;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GiftCardBundleChargeEtcResponseDto {

    @HashField(order = 1)
    private String gcDstbNo; // 유통관리번호

    @HashField(order = 2)
    private String useMid; // 사용처 상점 아이디

    @HashField(order = 3)
    @JsonProperty("mTrdNo")
    private String mTrdNo;

    @HashField(order = 4)
    private String dstbTrdNo;

    @HashField(order = 5)
    private String trdAmt;

    @HashField(order = 6)
    private String dstbBlc;

    @HashField(order = 7)
    private String chrgMeanCd;

    @HashField(order = 8)
    private String chrgTrdNo;
    private String trdDt;
    private String trdTm;
    private String trdSumry; // 거래적요

    @JsonProperty("mResrvField1")
    private String mResrvField1; // 예비필드1

    @JsonProperty("mResrvField2")
    private String mResrvField2; // 예비필드2

    @JsonProperty("mResrvField3")
    private String mResrvField3; // 예비필드3

    private String pktHash; // 해시 데이터
}
