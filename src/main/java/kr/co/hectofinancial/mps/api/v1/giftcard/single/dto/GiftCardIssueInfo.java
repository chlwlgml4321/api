package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GiftCardIssueInfo {

    private String gcAmt; // 금액
    private String gcQty; // 수량
    private String gcNoList; // 상품권 번호 목록
}
