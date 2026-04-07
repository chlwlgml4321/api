package kr.co.hectofinancial.mps.api.v1.trade.dto.wallet;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class WalletBalanceByLedgerResponseDto {
    private String custNo;
    private String chrgAmt;
    private String blcAmt;
    private String trdNo;
}
