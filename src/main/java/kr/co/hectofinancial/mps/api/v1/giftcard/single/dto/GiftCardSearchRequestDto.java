package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

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
public class GiftCardSearchRequestDto {

    @EncField
    @NotBlank(message = "상품권 정보")
    private String gcInfo; // 상품권 번호 또는 발행거래번호

    @NotBlank(message = "사용처 상점 아이디")
    private String useMid; // 사용처 상점 아이디

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String issDt; // 발행일자

    private Integer page; // 페이지 번호 (기본 : 1)
    private Integer size; // 페이지 크기 (기본 : 10)

    @NotLoggableParam
    private String clientIp;//TMS 로그 내 필요값 (요청 IP)

    @NotLoggableParam
    private long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)
}
