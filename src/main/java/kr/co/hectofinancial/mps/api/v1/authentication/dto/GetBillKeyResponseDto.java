package kr.co.hectofinancial.mps.api.v1.authentication.dto;

import kr.co.hectofinancial.mps.global.annotation.EncField;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class GetBillKeyResponseDto {

    private String custNo;
    @EncField
    private String billKey;
}
