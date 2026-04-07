package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search;

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
public class GiftCardBundleBalanceRequestDto {

    @NotBlank(message = "유통관리번호")
    private String gcDstbNo; // 유통관리번호

    @NotBlank(message = "사용처 상점 아이디")
    private String useMid; // 사용처 상점 아이디

    @NotLoggableParam
    private String clientIp;//TMS 로그 내 필요값 (요청 IP)

    @NotLoggableParam
    private long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)
}
