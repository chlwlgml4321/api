package kr.co.hectofinancial.mps.api.v1.trade.procedure.PayCancel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayCancelOut {
    //OUT 변수
    private Number outResCd;
    private String outResMsg;
    private Number outAmt;
    private Number outMnyBlc;
    private Number outPntBlc;
    private Number outWaitMnyBlc;
}