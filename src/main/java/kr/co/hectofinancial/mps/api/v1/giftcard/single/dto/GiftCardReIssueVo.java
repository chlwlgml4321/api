package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardReIssueVo {
    
    private String bfGcNoEnc; // 이전 상품권번호
    private String bfGcIssDt; // 이전 상품권 발행일자
    private String issAmt; // 발행금액
    private String useMid; // 사용처 상점 아이디
    private String vldDt; // 유효기간
    private String createDate; // 서버생성시간
}
