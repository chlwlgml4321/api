package kr.co.hectofinancial.mps.api.v1.trade.dto.wallet;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class WalletBalanceResponseDto {
    private String custNo;
    private String custDivCd;
    private String chrgblAmt;//충전가능금액
    private String mnyBlc;
    private String pntBlc;
    private String waitMnyBlc;
    private String expMnyBlc;//2M 이내 소멸예정 머니
//    private String expPntBlc;//3D 이내 소멸예정 포인트
}
