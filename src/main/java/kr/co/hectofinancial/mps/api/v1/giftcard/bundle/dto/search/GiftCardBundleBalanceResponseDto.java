package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GiftCardBundleBalanceResponseDto {

    private String gcDstbNo;
    private String useMid; // 사용처 상점 아이디
    private String dstbBlc;
    private String lastBlcUpdtDtm;
}
