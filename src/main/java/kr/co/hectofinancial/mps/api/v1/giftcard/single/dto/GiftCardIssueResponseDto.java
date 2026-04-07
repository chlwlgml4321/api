package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import lombok.*;

import java.util.List;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GiftCardIssueResponseDto {

    @HashField(order = 1)
    private String custNo; // 선불 회원 번호

    @HashField(order = 2)
    private String useMid; // 사용처 상점 아이디

    @HashField(order = 3)
    @JsonProperty("mTrdNo")
    private String mTrdNo; // 상점 거래 번호

    @HashField(order = 4)
    private String trdNo; // 발행 거래 번호

    private String vldDt; // 발행 만료 일자
    private String issDt; // 발행일자
    private String issTm; // 발행시간

    @HashField(order = 5)
    private String trdAmt; // 총 발행금액
    private String mnyAmt; // 발행에 사용된 머니 금액
    private String pntAmt; // 발행에 사용된 포인트 금액
    private String mnyBlc; // 발행 후 남은 머니 잔액
    private String pntBlc; // 발행 후 남은 포인트 잔액
    private String gcStatCd; // 상품권 상태
    private List<GiftCardIssueInfo> gcList; // 발행 결과
    private String trdSumry; // 거래적요

    @JsonProperty("mResrvField1")
    private String mResrvField1; // 예비필드1

    @JsonProperty("mResrvField2")
    private String mResrvField2; // 예비필드2

    @JsonProperty("mResrvField3")
    private String mResrvField3; // 예비필드3
    private String pktHash; // 해시 데이터
}
