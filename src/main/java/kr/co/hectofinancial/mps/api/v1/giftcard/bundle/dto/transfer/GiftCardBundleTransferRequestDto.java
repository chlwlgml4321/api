package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import kr.co.hectofinancial.mps.global.annotation.NotLoggableParam;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundleTransferRequestDto {

    @HashField(order = 1)
    @NotBlank(message = "유통관리번호")
    private String gcDstbNo; // 유통관리번호

    @HashField(order = 2)
    @NotBlank(message = "사용처 상점 아이디")
    private String useMid; // 상점 아이디

    @HashField(order = 3)
    @NotBlank(message = "상점거래번호")
    @JsonProperty("mTrdNo")
    private String mTrdNo; // 상점 거래 번호

    @EncField
    @HashField(order = 4)
    @NotBlank(message = "묶음 상품권 번호")
    private String gcBndlNo; // 묶음 상품권 번호

    @EncField
    @HashField(order = 5)
    @NotBlank(message = "거래금액")
    private String trdAmt; // 거래금액

    @EncField
    @HashField(order = 6)
    @NotBlank(message = "유통 잔액")
    private String dstbBlc; // 유통 잔액

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String reqDt; // 요청일자

    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    private String reqTm; // 요청시간

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String issDt; // 묶음상품권 발행일자

    @NotBlank(message = "해시데이터")
    private String pktHash; // 해시데이터

    @NotLoggableParam
    private String clientIp;//TMS 로그 내 필요값 (요청 IP)

    @NotLoggableParam
    private long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)
}
