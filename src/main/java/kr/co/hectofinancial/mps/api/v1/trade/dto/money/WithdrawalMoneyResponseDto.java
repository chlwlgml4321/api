package kr.co.hectofinancial.mps.api.v1.trade.dto.money;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class WithdrawalMoneyResponseDto {
    private String custNo;
    private String wdMnyAmt;
    private String mwCount;
}
