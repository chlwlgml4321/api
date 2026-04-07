package kr.co.hectofinancial.mps.api.v1.trade.dto.point;

import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoByMidRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetExpPntByMidRequestDto extends CommonInfoByMidRequestDto {

    @NotBlank(message = "조회 일자")
    @DateFormat(pattern = "yyyyMMdd", message = "조회 날짜 형식은 yyyyMMdd 입니다.")
    private String targetDate;

}
