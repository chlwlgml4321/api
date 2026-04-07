package kr.co.hectofinancial.mps.api.v1.firm.dto;

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

public class RemittanceResultDto {

    private String outStatCd;
    private String outRsltCd;
    private String outRsltMsg;
    private String trdNo;
}
