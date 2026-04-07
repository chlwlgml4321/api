package kr.co.hectofinancial.mps.api.v1.trade.dto.wallet;

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
public class WalletCancelRequestDto extends CommonLogicalRequestDto {
    @NotBlank(message = "원거래번호")
    @HashField(order = 3)
    private String orgTrdNo;
    @NotBlank(message = "원거래일자")
    @HashField(order = 4)
    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    private String orgTrdDt;
    @NotBlank(message = "머니 잔액")
    @EncField
    private String mnyBlc;

    @NotBlank(message = "포인트 잔액")
    @EncField
    private String pntBlc;

    @NotBlank(message = "취소 머니 금액")
    @EncField
    private String cnclMnyAmt;

    @NotBlank(message = "취소 포인트 금액")
    @EncField
    private String cnclPntAmt;
    private String trdSumry;
    @JsonProperty("mResrvField1")
    private String mResrvField1;
    @JsonProperty("mResrvField2")
    private String mResrvField2;
    @JsonProperty("mResrvField3")
    private String mResrvField3;
}
