package kr.co.hectofinancial.mps.global.constant;

public enum MpsPrdtCd {

    charge("PCHRG"),

    use("PUSE"),
    withdrawal("PWD"),
    ;

    private final String prdtCd;

    MpsPrdtCd(String prdtCd) {
        this.prdtCd = prdtCd;
    }

    public String getPrdtCd() {
        return prdtCd;
    }
}
