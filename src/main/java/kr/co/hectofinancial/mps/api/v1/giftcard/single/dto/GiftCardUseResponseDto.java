package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import lombok.*;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GiftCardUseResponseDto {

    @HashField(order = 1)
    @JsonProperty("mTrdNo")
    private String mTrdNo; // 상점 거래 번호

    @HashField(order = 2)
    private String gcTrdNo; // 상점 거래 번호

    @HashField(order = 3)
    private String useMid; // 사용처 상점 아이디

    @HashField(order = 4)
    private String useGcList; // 사용 상품권 목록

    private String gcStatCd; // 상품권 상태

    private String trdAmt; // 총 사용금액

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String trdDt; // 거래일자

    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    private String trdTm; // 거래시간

    private String trdSumry; // 거래적요

    @JsonProperty("mResrvField1")
    private String mResrvField1; // 예비필드1

    @JsonProperty("mResrvField2")
    private String mResrvField2; // 예비필드2

    @JsonProperty("mResrvField3")
    private String mResrvField3; // 예비필드3

    private String pktHash; // 해시 데이터
}
