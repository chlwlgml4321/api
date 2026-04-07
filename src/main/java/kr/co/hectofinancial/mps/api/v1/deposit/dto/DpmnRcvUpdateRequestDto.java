package kr.co.hectofinancial.mps.api.v1.deposit.dto;

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

public class DpmnRcvUpdateRequestDto {

    private String trdDt;
    private String mid;
    private long dpAmt;
    private String globalId;

}
