package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GiftCardBundleListResponseDto {

    private String totCnt; // 상품권 정보 총건수
    private String totPage; // 총 페이지
    private String curPage; // 현재 페이지
    private String useMid; // 사용처 상점 아이디
    private String gcDstbNo;
    private List<GiftCardBundleInfo> gcBndlList; // 상품권 정보
}
