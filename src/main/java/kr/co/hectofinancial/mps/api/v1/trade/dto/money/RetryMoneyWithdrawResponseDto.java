package kr.co.hectofinancial.mps.api.v1.trade.dto.money;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class RetryMoneyWithdrawResponseDto {

    private String outStatCd; //성공: 0021, 실패: 0031
    private String orgTrdNo;

}
