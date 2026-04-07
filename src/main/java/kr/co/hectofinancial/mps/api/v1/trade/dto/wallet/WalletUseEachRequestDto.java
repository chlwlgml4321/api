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
public class WalletUseEachRequestDto extends CommonLogicalRequestDto {

    @HashField(order = 3)
    @EncField
    @NotBlank(message = "사용 요청 머니금액")
    private String mnyAmt;
    @HashField(order = 4)
    @EncField
    @NotBlank(message = "사용 요청 포인트금액")
    private String pntAmt;
    @NotBlank(message = "머니 잔액")
    @EncField
    private String mnyBlc;
    @NotBlank(message = "포인트 잔액")
    @EncField
    private String pntBlc;

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt;
    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm;
    private String csrcIssReqYn; //현금영수증 발행 여부
    @EncField(nullable = true)
    private String stlMId; //정산 상점 아이디
    private String storCd; //사용처 코드
    private String storNm; //사용처 명
    @NotBlank(message = "결제비밀번호")
    @EncField
    private String pinNo;
    @JsonProperty("mResrvField1")
    public String mResrvField1;
    @JsonProperty("mResrvField2")
    public String mResrvField2;
    @JsonProperty("mResrvField3")
    public String mResrvField3;

}
