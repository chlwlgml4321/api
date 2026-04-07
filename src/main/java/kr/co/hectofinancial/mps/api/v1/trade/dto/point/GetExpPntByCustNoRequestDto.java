package kr.co.hectofinancial.mps.api.v1.trade.dto.point;

import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetExpPntByCustNoRequestDto extends CommonInfoRequestDto {

    @NotBlank(message = "조회 일자")
    @DateFormat(pattern = "yyyyMMdd", message = "조회 날짜 형식은 yyyyMMdd 입니다.")
    private String targetDate;

    @NotBlank(message = "선불 회원 CI 값")
    @EncField
    private String ci;
}
