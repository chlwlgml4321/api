package kr.co.hectofinancial.mps.api.v1.trade.dto.money;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class WillMnyWithdrawalYnResponseDto {
    private String custNo;
    private String mid;
    private String wdMnyYn;

    public void saveWdMny(String custNo, String mid, String wdMnyYn){
        this.custNo = custNo;
        this.mid = mid;
        this.wdMnyYn = wdMnyYn;
    }
}
