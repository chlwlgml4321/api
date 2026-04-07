package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

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
public class GiftCardReissueRequestDto {

    @HashField(order = 1)
    @NotBlank(message = "사용처 상점 아이디")
    private String useMid; // 사용처 상점 아이디

    @HashField(order = 2)
    @EncField
    @NotBlank(message = "상품권 번호")
    private String gcNo; // 상품권 번호

    @HashField(order = 3)
    @EncField
    @NotBlank(message = "상품권 금액")
    private String gcAmt; // 상품권 금액

    @NotBlank(message = "해시 데이터")
    private String pktHash; // 해시데이터

    @NotLoggableParam
    private String clientIp;//TMS 로그 내 필요값 (요청 IP)

    @NotLoggableParam
    private long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)
}
