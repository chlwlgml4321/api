package kr.co.hectofinancial.mps.api.v1.trade.procedure.UseCancel;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UseCancelOut {
    private long outResCd;
    private String outResMsg;
    private long outMnyAmt; //취소 머니
    private long outPntAmt; //취소 포인트
    private long outExprPntAmt; //만료 포인트 (취소 포인트 안에 포함)
    private long outWaitMnyAmt; //대기머니
    private long outMnyBlc; //머니 잔액
    private long outPntBlc; //포인트 잔액
    private long outWaitMnyBlc; //대기머니 잔액

    @Builder
    public UseCancelOut(Long outResCd, String outResMsg, Long outMnyAmt, Long outPntAmt, Long outExprPntAmt, Long outWaitMnyAmt, Long outMnyBlc, Long outPntBlc, Long outWaitMnyBlc) {
        this.outResCd = outResCd == null ? 9999 : outResCd;
        this.outResMsg = outResMsg == null ? "" : outResMsg;
        this.outMnyAmt = outMnyAmt == null ? 0L : outMnyAmt;
        this.outPntAmt = outPntAmt == null ? 0L : outPntAmt;
        this.outExprPntAmt = outExprPntAmt == null ? 0L : outExprPntAmt;
        this.outWaitMnyAmt = outWaitMnyAmt == null ? 0L : outWaitMnyAmt;
        this.outMnyBlc = outMnyBlc == null ? 0L : outMnyBlc;
        this.outPntBlc = outPntBlc == null ? 0L : outPntBlc;
        this.outWaitMnyBlc = outWaitMnyBlc == null ? 0L : outWaitMnyBlc;
    }
}