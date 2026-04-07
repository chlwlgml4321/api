package kr.co.hectofinancial.mps.api.v1.trade.dto.point;

import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class PointExpireRequestDto extends CommonInfoRequestDto {

    private long expPntAmt;
    private String cnclTrdNo;
    private String cnclTrdDt;
    private String reqDt;
    private String reqTm;
    private String orgTrdNo;
    private String orgTrdDt;
}
