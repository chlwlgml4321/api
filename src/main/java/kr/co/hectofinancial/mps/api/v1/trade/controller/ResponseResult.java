package kr.co.hectofinancial.mps.api.v1.trade.controller;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class ResponseResult<O> {
    private String rsltCd;			// 결과코드
    private String rsltMsg;		// 결과메시지
    private O rsltObj;				// 결과객체

    public ResponseResult(String rsltCd, String rsltMsg, O rsltObj) {
        super();
        this.rsltCd = rsltCd;
        this.rsltMsg = rsltMsg;
        this.rsltObj = rsltObj;
    }
}
