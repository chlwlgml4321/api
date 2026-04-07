package kr.co.hectofinancial.mps.api.v1.trade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * 사용 거래 내역 금액 합계 api ResponseDto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class TradeUseSummaryResponseDto {
    private String custNo;
    private String period;
    private String trdCnt;
    private String trdAmt;
    private String mnyAmt;
    private String pntAmt;
}
