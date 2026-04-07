package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import kr.co.hectofinancial.mps.global.annotation.NotLoggableParam;
import lombok.*;

import javax.validation.constraints.*;
import java.util.List;

@Data
@Builder(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundleIssueRequestDto {

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

    @HashField(order = 4)
    @EncField
    @NotBlank(message = "발행금액")
    private String trdAmt; // 발행금액

    @HashField(order = 5)
    @EncField
    @NotBlank(message = "유통잔액")
    private String dstbBlc; // 유통잔액

    private List<GiftCardIssueInfo> issList; // 상품권 발행 요청 정보

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String reqDt; // 요청일자

    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    private String reqTm; // 요청시간

    @NotBlank(message = "해시데이터")
    private String pktHash; // 해시데이터

    @NotLoggableParam
    private String clientIp;//TMS 로그 내 필요값 (요청 IP)

    @NotLoggableParam
    private long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)
}
