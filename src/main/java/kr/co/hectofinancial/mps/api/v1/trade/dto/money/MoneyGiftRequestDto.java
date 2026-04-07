package kr.co.hectofinancial.mps.api.v1.trade.dto.money;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonLogicalRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MoneyGiftRequestDto extends CommonLogicalRequestDto {
    @NotBlank(message = "거래 구분 코드")
    private String divCd;
    @NotBlank(message = "수신 선불 회원 번호")
    private String resCustNo;
    @HashField(order = 3)
    @EncField
    @NotBlank(message = "거래 금액")
    private String trdAmt;
    @NotBlank(message = "머니 잔액")
    @EncField
    private String mnyBlc;
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt;
    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm;
    @NotBlank(message = "결제비밀번호")
    @EncField
    public String pinNo;
    @JsonProperty("mResrvField1")
    public String mResrvField1;
    @JsonProperty("mResrvField2")
    public String mResrvField2;
    @JsonProperty("mResrvField3")
    public String mResrvField3;
}
