package kr.co.hectofinancial.mps.api.v1.common.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetCustChrgMeanResponseDto {

    private String custNo;
    private String chrgMeanCd;
    private String bankCd;
    private String accountNo;
    private String aesAccountNo;
    private String custNm;
    private String birthDt;
    private String bizRegNo;

}
