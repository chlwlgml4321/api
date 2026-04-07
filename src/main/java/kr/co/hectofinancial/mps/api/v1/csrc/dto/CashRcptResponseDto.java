package kr.co.hectofinancial.mps.api.v1.csrc.dto;

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
public class CashRcptResponseDto {
    private int totCnt;
    private int sucCnt;
    private int errCnt;
    private int registCnt;
    private int cancelCnt;

}
