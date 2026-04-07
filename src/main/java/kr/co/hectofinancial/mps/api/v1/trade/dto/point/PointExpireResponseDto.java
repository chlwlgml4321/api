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
public class PointExpireResponseDto extends CommonInfoRequestDto {

    private long expPntAmt;
    private long pntBlc;
    private String expTrdDt;
    private String expTrdTm;



}
