package kr.co.hectofinancial.mps.api.v1.trade.dto.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class WalletBalanceByLedgerRequestDto extends CommonInfoRequestDto {
    @EncField(nullable = true)
    @NotBlank(message = "ci")
    @JsonProperty("ci")
    private String ci;
    
    @NotBlank(message = "선불 회원 번호")
    @JsonProperty("custNo")
    public String custNo;
    
    @NotBlank(message = "선불 승인 번호")
    @JsonProperty("trdNo")
    public String trdNo;

    @NotBlank(message = "거래 일자")
    @JsonProperty("trdDt")
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String trdDt;
    
    @NotBlank(message = "잔액 구분 코드 'P'/'M' ")
    @JsonProperty("blcDivCd")
    public String blcDivCd;


}
