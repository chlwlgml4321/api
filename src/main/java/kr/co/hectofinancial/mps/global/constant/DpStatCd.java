package kr.co.hectofinancial.mps.global.constant;

/**
 * 선불예치금 납부 구분코드
 */
public enum DpStatCd {

    /*
    01: 완납
    02: 미납
    03: 과납
     */
    FULL_PAYMENT("01"),
    UNDER_PAYMENT("02"),
    OVER_PAYMENT("03");
    private final String dpStatCd;

    DpStatCd(String dpStatCd) {
        this.dpStatCd = dpStatCd;
    }

    public String getDpStatCd() {
        return dpStatCd;
    }
}
