package kr.co.hectofinancial.mps.api.v1.trade.procedure.Pay;

public interface Pay {
    PayOut doPay(PayIn params);
}
