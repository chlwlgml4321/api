package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardSearchResult {

    private long totCnt; // 총 건수
    private int totPage; // 총 페이지
    private int curPage; // 현재 페이지
    private List<GiftCardIssue> gcIssList; // 조회 정보
}
