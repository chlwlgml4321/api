package kr.co.hectofinancial.mps.api.v1.trade.dto.wallet;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class CustomerWalletResponseDto {
    private String custNo;
    private String custDivCd;
    private long chrgblAmt;//충전가능금액
    private long mnyBlc;
    private long pntBlc;
    private long waitMnyBlc;
    private long expMnyBlc;//2M 이내 소멸예정 머니
//    private long expPntBlc;//3D 이내 소멸예정 포인트
}
