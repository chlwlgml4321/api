package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.cancel;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import kr.co.hectofinancial.mps.global.annotation.NotLoggableParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundleChargeCancelRequestDto {

    @HashField(order = 1)
    @NotBlank(message = "유통관리번호")
    private String gcDstbNo; // 유통관리번호

    @HashField(order = 2)
    @NotBlank(message = "사용처 상점 아이디")
    private String useMid; // 사용처 상점 아이디

    @HashField(order = 3)
    @NotBlank(message = "상점거래번호")
    @JsonProperty("mTrdNo")
    private String mTrdNo; // 상점 거래 번호

    @HashField(order = 4)
    @NotBlank(message = "원거래 승인 번호")
    private String orgDstbTrdNo; // 원거래 승인 번호

    @HashField(order = 5)
    @NotBlank(message = "원거래 일자")
    private String orgTrdDt; // 원거래 일자

    @EncField
    @HashField(order = 6)
    @NotBlank(message = "충전취소 금액")
    private String trdAmt; // 충전취소 금액

    @EncField
    @HashField(order = 7)
    @NotBlank(message = "유통 잔액")
    private String dstbBlc; // 유통 잔액

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt; // 요청일자

    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm; // 요청시간

    private String trdSumry; // 거래적요

    @JsonProperty("mResrvField1")
    private String mResrvField1; // 예비필드1

    @JsonProperty("mResrvField2")
    private String mResrvField2; // 예비필드2

    @JsonProperty("mResrvField3")
    private String mResrvField3; // 예비필드3

    @NotBlank(message = "해시데이터")
    private String pktHash; // 해시데이터

    @NotLoggableParam
    private CustomerDto customerDto;

    @NotLoggableParam
    private String clientIp;//TMS 로그 내 필요값 (요청 IP)

    @NotLoggableParam
    private long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)
}
