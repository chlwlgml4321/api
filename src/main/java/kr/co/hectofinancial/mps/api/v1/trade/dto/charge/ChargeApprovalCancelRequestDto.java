package kr.co.hectofinancial.mps.api.v1.trade.dto.charge;

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

public class ChargeApprovalCancelRequestDto extends CommonLogicalRequestDto {

    @NotBlank(message = "원거래번호")
    @HashField(order = 3)
    public String orgTrdNo;
    @NotBlank(message = "원거래일자")
    @HashField(order = 4)
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String orgTrdDt;
    @NotBlank(message = "거래 구분 코드")
    public String divCd;
    @EncField
    @NotBlank(message = "머니/포인트 잔액")
    private String blcAmt;

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt;
    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm;
    public String trdSumry;
    @JsonProperty("mResrvField1")
    public String mResrvField1;
    @JsonProperty("mResrvField2")
    public String mResrvField2;
    @JsonProperty("mResrvField3")
    public String mResrvField3;
    @EncField(nullable = true)
    private String cnclAmt; //부분취소 요청 금액(머니, 포인트 모두가능)
}
