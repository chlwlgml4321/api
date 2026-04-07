package kr.co.hectofinancial.mps.api.v1.trade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.*;

/**
 * 거래내역조회 (List) API 의 요청 Dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TradeInfoListRequestDto extends CommonInfoRequestDto {
    @NotBlank(message = "선불 회원 CI 값")
    @EncField
    private String ci;
    @DateFormat(pattern = "yyyyMM", message = "조회 기간 형식은 yyyyMM 입니다.")
    private String period;
    @Min(value = 1, message = "페이지는 1보다 작을 수 없습니다")
    private int page = 1;

    @Min(value = 10, message = "한 페이지 사이즈는 10보다 작을 수 없습니다")
    @Max(value = 100, message = "한 페이지 사이즈는 100보다 클 수 없습니다")
    private int size = 10;
    @JsonProperty("trdDivCd")
    private String trdDivCd; //거래구분코드
    @JsonProperty("mTrdNo")
    private String mTrdNo;//상점거래번호
    @JsonProperty("trdNo")
    private String trdNo;//선불거래번호
    @JsonProperty("showCnclYn")
    private String showCnclYn = "N";//취소거래포함여부
    @JsonProperty("blcDivCd")
    private String blcDivCd; //잔액거래구분코드 (M:머니, 대기머니, P:포인트)
    @JsonProperty("cardTrdOnlyYn")
    private String cardTrdOnlyYn; //카드거래건만
}
