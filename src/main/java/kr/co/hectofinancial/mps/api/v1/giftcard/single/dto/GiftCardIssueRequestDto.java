package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

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
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardIssueRequestDto {

    @HashField(order = 1)
    @NotBlank(message = "회원번호")
    private String custNo; // 회원번호

    @HashField(order = 2)
    @NotBlank(message = "상점거래번호")
    @JsonProperty("mTrdNo")
    private String mTrdNo; // 상점 거래 번호

    @HashField(order = 3)
    @NotBlank(message = "사용처 상점 아이디")
    private String useMid; // 사용처 상점 아이디

    @HashField(order = 4)
    @EncField
    @NotBlank(message = "총 발행금액")
    private String trdAmt; // 총 발행금액

    @EncField
    @NotBlank(message = "총 발행수량")
    private String totCnt; // 총 발행수량

    @NotEmpty(message = "상품권 발행 요청 정보")
    private List<GiftCardIssueInfo> issList; // 상품권 발행 요청 정보

    @EncField
    @NotBlank(message = "머니 잔액")
    private String mnyBlc; // 머니 잔액

    @EncField
    @NotBlank(message = "포인트 잔액")
    private String pntBlc; // 포인트 잔액

    @EncField
    @NotBlank(message = "결제 핀 번호")
    private String pinNo; // 결제 핀 번호

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String vldDt; // 만료일자

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt; // 요청일자

    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm; // 요청시간

    private String blcUseOrd; // 잔액 사용 순서 (기본 P)
    private String trdSumry; // 거래적요

    @JsonProperty("mResrvField1")
    private String mResrvField1; // 예비필드1

    @JsonProperty("mResrvField2")
    private String mResrvField2; // 예비필드2

    @JsonProperty("mResrvField3")
    private String mResrvField3; // 예비필드3

    @NotBlank(message = "해시데이터")
    public String pktHash; // 해시데이터

    //API 요청값은 아니나, AOP 회원 검증 후 유효한 회원값 담아주는 변수
    @NotLoggableParam
    private CustomerDto customerDto;

    @NotLoggableParam
    private String clientIp;//TMS 로그 내 필요값 (요청 IP)

    @NotLoggableParam
    private long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)
}
