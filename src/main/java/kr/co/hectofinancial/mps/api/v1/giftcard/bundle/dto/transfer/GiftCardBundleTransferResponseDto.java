package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import lombok.*;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundleTransferResponseDto {

    @HashField(order = 1)
    private String gcDstbNo; // 유통관리번호

    @HashField(order = 2)
    private String useMid; // 상점 아이디

    @HashField(order = 3)
    @JsonProperty("mTrdNo")
    private String mTrdNo; // 상점 거래 번호

    @HashField(order = 4)
    private String dstbTrdNo; // 유통 거래 번호

    @EncField
    @HashField(order = 5)
    private String gcBndlNo; // 묶음 상품권 번호

    @HashField(order = 6)
    private String gcBndlStatCd; // 묶음 상품권 상태

    @HashField(order = 7)
    private String trdAmt; // 거래금액

    @HashField(order = 8)
    private String dstbBlc; // 유통잔액
    private String procDt; // 처리일자
    private String procTm; // 처리시간
    private String issDt; // 묶음상품권 발행일자
    private String pktHash; // 해시 데이터
}
