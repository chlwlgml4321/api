package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundleHistoryInfo {

    private String type;
    private String procDt;
    private String procTm;
}
