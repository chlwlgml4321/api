package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search;

import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
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
public class GiftCardBundleInfoRequestDto {

    @NotBlank(message = "유통관리번호")
    private String gcDstbNo; // 유통관리번호

    @EncField
    @NotBlank(message = "묶음 상품권 번호")
    private String gcBndlNo; // 묶음 상품권 번호

    @NotBlank(message = "사용처 상점 아이디")
    private String useMid; // 상점 아이디

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String issDt; // 묶음상품권 발행일자

    @NotLoggableParam
    private String clientIp;//TMS 로그 내 필요값 (요청 IP)

    @NotLoggableParam
    private long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)
}
