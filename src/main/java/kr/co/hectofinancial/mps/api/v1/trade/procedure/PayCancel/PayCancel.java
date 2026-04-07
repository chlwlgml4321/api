package kr.co.hectofinancial.mps.api.v1.trade.procedure.PayCancel;

public interface PayCancel {
    PayCancelOut doPayCancel(PayCancelIn params);
}
