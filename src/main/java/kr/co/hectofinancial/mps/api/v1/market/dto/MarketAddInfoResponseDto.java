package kr.co.hectofinancial.mps.api.v1.market.dto;

import kr.co.hectofinancial.mps.api.v1.market.domain.MarketAddInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MarketAddInfoResponseDto {
    private String mid;
    private LocalDateTime stDate;
    private LocalDateTime edDate;
    private String encKey;
    private String encIv;
    private String encMthdCd;
    private String pktHashKey;

    public static MarketAddInfoResponseDto of(MarketAddInfo marketAddInfo) {
        return MarketAddInfoResponseDto.builder()
                .mid(marketAddInfo.getMid())
                .stDate(marketAddInfo.getStDate())
                .edDate(marketAddInfo.getEdDate())
                .encKey(marketAddInfo.getEncKey())
                .encIv(marketAddInfo.getEncIv())
                .encMthdCd(marketAddInfo.getEncMthdCd())
                .pktHashKey(marketAddInfo.getPktHashKey())
                .build();
    }
}
