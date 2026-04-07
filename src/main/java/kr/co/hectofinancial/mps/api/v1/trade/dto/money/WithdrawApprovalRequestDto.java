package kr.co.hectofinancial.mps.api.v1.trade.dto.money;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonLogicalRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class WithdrawApprovalRequestDto extends CommonLogicalRequestDto {

    @NotBlank(message = "거래 구분 코드")
    public String divCd;
    @EncField
    @HashField(order = 3)
    @NotBlank(message = "거래 금액")
    public String trdAmt;
    @EncField(nullable = true)
    public String custBdnFeeAmt;
    @EncField
    @NotBlank(message = "머니 잔액")
    public String mnyBlc;
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt;
    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm;
    @EncField
    @NotBlank(message = "결제비밀번호")
    private String pinNo;
    @JsonProperty("mResrvField1")
    public String mResrvField1;
    @JsonProperty("mResrvField2")
    public String mResrvField2;
    @JsonProperty("mResrvField3")
    public String mResrvField3;

}
