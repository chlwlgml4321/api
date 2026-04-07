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
public class ChargeApprovalRequestDto extends CommonLogicalRequestDto {
    @NotBlank(message = "충전 수단 코드")
    public String chrgMeanCd;
    @EncField
    @HashField(order = 3)
    @NotBlank(message = "거래 금액")
    public String trdAmt;

    @NotBlank(message = "거래 구분 코드")
    public String divCd;
    @EncField
    @NotBlank(message = "머니/포인트 잔액")
    public String blcAmt;
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String pntVldPd;
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt;
    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm;
    public String trdSumry;
    @EncField(nullable = true)
    public String custBdnFeeAmt;
    public String chrgTrdNo; //외부 충전거래번호 PG는 필수 값
    public String trdDivDtlCd;
    @JsonProperty("mResrvField1")
    public String mResrvField1;
    @JsonProperty("mResrvField2")
    public String mResrvField2;
    @JsonProperty("mResrvField3")
    public String mResrvField3;

}
