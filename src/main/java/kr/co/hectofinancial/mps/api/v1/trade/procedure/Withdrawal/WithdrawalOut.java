package kr.co.hectofinancial.mps.api.v1.trade.procedure.Withdrawal;

import lombok.Builder;
import lombok.Getter;

@Getter
public class WithdrawalOut {
    //OUT 변수
    private long outResCd;
    private String outResMsg;
    private long outMnyAmt;
    private long outPntAmt;
    private long outMnyBlc;
    private long outPntBlc;
    private long outWaitMnyBlc;

    @Builder
    public WithdrawalOut(Long outResCd, String outResMsg, Long outMnyAmt, Long outPntAmt, Long outMnyBlc, Long outPntBlc, Long outWaitMnyBlc) {
        this.outResCd = outResCd == null ? 9999 : outResCd;
        this.outResMsg = outResMsg == null ? "" : outResMsg;
        this.outMnyAmt = outMnyAmt == null ? 0L : outMnyAmt;
        this.outPntAmt = outPntAmt == null ? 0L : outPntAmt;
        this.outMnyBlc = outMnyBlc == null ? 0L : outMnyBlc;
        this.outPntBlc = outPntBlc == null ? 0L : outPntBlc;
        this.outWaitMnyBlc = outWaitMnyBlc == null ? 0L : outWaitMnyBlc;
    }
}