package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardSearch {

    private String gcNo; // 상품권 번호
    private String gcAmt; // 상품권 금액
    private String gcStatCd; // 상품권 상태
    private String vldDt; // 상품권 만료일자
    private List<GiftCardHistoryInfo> histList; // 상품권 이력
}
