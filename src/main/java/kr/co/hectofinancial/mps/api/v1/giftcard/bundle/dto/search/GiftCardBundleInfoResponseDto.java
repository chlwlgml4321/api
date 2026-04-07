package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import lombok.*;

import java.util.List;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundleInfoResponseDto {

    private String gcDstbNo;
    private String useMid;

    @EncField
    private String gcBndlNo;
    private String gcBndlStatCd;
    private String issDt;
    private String issTm;
    private String vldDt;
    private List<GiftCardIssueInfo> issList;
    private List<GiftCardBundleHistoryInfo> histList;
}
