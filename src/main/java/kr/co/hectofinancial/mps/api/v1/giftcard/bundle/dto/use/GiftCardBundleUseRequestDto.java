package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import kr.co.hectofinancial.mps.global.annotation.NotLoggableParam;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@Builder(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundleUseRequestDto {

    @HashField(order = 1)
    @NotBlank(message = "상점 거래 번호")
    @JsonProperty(value = "mTrdNo")
    private String mTrdNo; // 상점 거래 번호

    @HashField(order = 2)
    @NotBlank(message = "사용처 상점 아이디")
    private String useMid; // 사용처 상점 아이디

    @EncField
    @HashField(order = 3)
    @NotBlank(message = "묶음 상품권 번호")
    private String gcBndlNo; // 묶음 상품권 번호

    @EncField
    @HashField(order = 4)
    @NotBlank(message = "묶음 상품권 금액")
    private String trdAmt; // 묶음 상품권 금액

    @EncField
    @HashField(order = 5)
    @NotBlank(message = "정산 상점 아이디")
    private String stlMid; // 정산 상점 아이디

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String trdDt; // 거래일자

    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    private String trdTm; // 거래시간

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String issDt; // 묶음상품권 발행일자

    private String trdSumry; // 거래 적요

    @JsonProperty("mResrvField1")
    private String mResrvField1; // 예비필드1

    @JsonProperty("mResrvField2")
    private String mResrvField2; // 예비필드2

    @JsonProperty("mResrvField3")
    private String mResrvField3; // 예비필드3

    @NotBlank(message = "해시 데이터")
    private String pktHash; // 해시데이터

    @NotLoggableParam
    private String clientIp;//TMS 로그 내 필요값 (요청 IP)

    @NotLoggableParam
    private long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)
}
