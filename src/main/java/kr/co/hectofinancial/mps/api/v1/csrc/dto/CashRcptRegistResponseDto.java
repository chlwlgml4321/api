package kr.co.hectofinancial.mps.api.v1.csrc.dto;

import kr.co.hectofinancial.mps.global.annotation.HashField;
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
public class CashRcptRegistResponseDto {

    private String custNo;
    private int totCnt;
    private int sucCnt;

}
