package kr.co.hectofinancial.mps.api.v1.card.dto;

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
public class CardUseApprovalRequestDto extends CommonLogicalRequestDto {
    @HashField(order = 3)
    @EncField
    @NotBlank(message = "거래 금액")
    private String trdAmt;
    @NotBlank(message = "머니 잔액")
    @EncField
    private String mnyBlc;
    @NotBlank(message = "포인트 잔액")
    @EncField
    private String pntBlc;
    private String blcUseOrd; //잔액 사용 순서

    @DateFormat(pattern = "yyyyMMdd", message = "날짜형식은 yyyyMMdd 입니다.")
    public String reqDt;
    @DateFormat(pattern = "HHmmss", message = "시간형식은 HHmmss 입니다.")
    public String reqTm;
    private String csrcIssReqYn; //현금영수증 발행 여부
    private String storCd; //사용처 코드
    private String storNm; //사용처 명
    private String trdSumry;
    @JsonProperty("mResrvField1")
    private String mResrvField1;
    @JsonProperty("mResrvField2")
    private String mResrvField2;
    @JsonProperty("mResrvField3")
    private String mResrvField3;
    @JsonProperty("cardMngNo")
//    @NotBlank(message = "카드관리번호")
    private String cardMngNo;
    @JsonProperty("cnclTypeCd")
    private String cnclTypeCd; //BPO 카드 재발급일때만 "1"
    @JsonProperty("stlMId")
    @NotBlank(message = "정산상점아이디")
    private String stlMId; //정산 상점 아이디
}
