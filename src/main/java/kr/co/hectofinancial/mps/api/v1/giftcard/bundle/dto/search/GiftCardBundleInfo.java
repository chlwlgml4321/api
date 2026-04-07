package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundleInfo {

    private String gcBndlNo;
    private String gcBndlStatCd;
    private String gcBndlAmt;
    private String issDt;
    private List<GiftCardIssueInfo> issList;
}
