package kr.co.hectofinancial.mps.global.constant;

/**
 * TRADE 테이블
 * blcUseOrd
 */
public enum TrBlcUseOrd {
    //M: 머니, P: 포인트, W: 대기머니
    MONEY("M"), POINT("P"), WAIT("W");
    private final String blcUseOrd;

    TrBlcUseOrd(String blcUseOrd) {
        this.blcUseOrd = blcUseOrd;
    }

    public String getBlcUseOrd() {
        return this.blcUseOrd;
    }
}
