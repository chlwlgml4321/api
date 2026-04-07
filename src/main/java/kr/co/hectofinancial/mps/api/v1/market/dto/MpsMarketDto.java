package kr.co.hectofinancial.mps.api.v1.market.dto;

import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarket;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MpsMarketDto {
    private String mid;
    private String useYn;
    private String rmk;
    private String custJoinTypeCd;
    private long monWdLmtCnt;
    private String wdTypeCd;
    private String billKeyUseYn;
    private String custBizDivCd;
    private String pinVrifyYn;
    private String pinVrifyTypeCd;
    private String wdTrdSumry;

    public static MpsMarketDto of (MpsMarket mpsMarket) {
        return MpsMarketDto.builder()
                .mid(mpsMarket.getMid())
                .useYn(mpsMarket.getUseYn())
                .rmk(mpsMarket.getRmk())
                .custJoinTypeCd(mpsMarket.getCustJoinTypeCd())
                .monWdLmtCnt(mpsMarket.getMonWdLmtCnt())
                .wdTypeCd(mpsMarket.getWdTypeCd())
                .billKeyUseYn(mpsMarket.getBillKeyUseYn())
                .custBizDivCd(mpsMarket.getCustBizDivCd())
                .pinVrifyYn(mpsMarket.getPinVrifyYn())
                .pinVrifyTypeCd(mpsMarket.getPinVrifyTypeCd())
                .wdTrdSumry(mpsMarket.getWdTrdSumry())
                .build();
    }
}
