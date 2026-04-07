package kr.co.hectofinancial.mps.api.v1.trade.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class TradeFindByTrDivCdCountRequestDto {

    private String custNo;
    private String divCd;
    private String targetMonth;

}
