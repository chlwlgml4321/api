package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import lombok.*;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GiftCardReissueResponseDto {

    @HashField(order = 1)
    private String useMid; // 사용처 상점 아이디

    @EncField
    @HashField(order = 2)
    private String gcNo; // 신규 상품권 번호

    private String gcStatCd; // 신규 상품권 상태

    @EncField
    @HashField(order = 3)
    private String bfGcNo; // 이전 상품권 번호

    private String bfGcStatCd; // 이전 상품권 상태

    @HashField(order = 4)
    private String gcAmt; // 상품권 금액

    private String vldDt; // 상품권 만료일자
    private String procDt; // 처리일자
    private String procTm; // 처리시간
    private String pktHash; // 해시데이터
}
