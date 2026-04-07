package kr.co.hectofinancial.mps.api.v1.trade.dto.wallet;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class CustomerWalletDtlResponseDto {
    private String custNo;
    private String custDivCd;
    private String chrgblAmt;
    private String mnyBlc;
    private String pntBlc;
    private String waitMnyBlc;
    private Map<String, List<Map<String, String>>> data;
}
