package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GiftCardHistoryInfo {

    private String type; // 구분 - 발행/사용/교체/만료/취소
    private String date; // 일자
    private String time; // 시간
    private String trdNo; // 발행, 사용 또는 취소 거래 번호
    private String trdSumry; // 거래적요

    @JsonProperty("mResrvField1")
    private String mResrvField1; // 예비필드1

    @JsonProperty("mResrvField2")
    private String mResrvField2; // 예비필드2

    @JsonProperty("mResrvField3")
    private String mResrvField3; // 예비필드3

    private String newGcNo; // 신규 상품권 번호 (교체인 경우에만 존재)
}
