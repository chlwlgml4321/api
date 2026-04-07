package kr.co.hectofinancial.mps.api.v1.trade.dto.admin;

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
public class AdminWithdrawApprovalRequestDto extends CommonLogicalRequestDto {

    @NotBlank(message = "거래 구분 코드, 머니출금/대기머니출금")
    public String divCd;
    @EncField
    @HashField(order = 3)
    @NotBlank(message = "거래 금액")
    public String trdAmt;
    @EncField
    @NotBlank(message = "잔액")
    public String blcAmt;
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt;
    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm;
    @JsonProperty("mResrvField1")
    public String mResrvField1;
    @JsonProperty("mResrvField2")
    public String mResrvField2;
    @JsonProperty("mResrvField3")
    public String mResrvField3;
    public String trdSumry;

}
