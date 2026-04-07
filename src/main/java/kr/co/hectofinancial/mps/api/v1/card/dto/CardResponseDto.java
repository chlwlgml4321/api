package kr.co.hectofinancial.mps.api.v1.card.dto;

import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoByMidRequestDto;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonLogicalResponseDto;
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
public class CardResponseDto extends CommonInfoByMidRequestDto {
    String mpsCustNo;

}
