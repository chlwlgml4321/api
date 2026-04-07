package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import lombok.*;

import java.util.List;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardSearchResponseDto {

    private String totCnt; // 상품권 정보 총건수
    private String totPage; // 총 페이지
    private String curPage; // 현재 페이지
    private String useMid; // 사용처 상점 아이디
    private List<GiftCardSearch> gcList; // 상품권 정보
}
