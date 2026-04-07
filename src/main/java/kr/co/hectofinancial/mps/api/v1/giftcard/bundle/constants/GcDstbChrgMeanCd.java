package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants;

public enum GcDstbChrgMeanCd {

    MPS("MPS"),
    EZ("EZ"),
    CREDIT_CARD("CA"),
    TRANSFER("TX")
    ;

    private final String code;
    GcDstbChrgMeanCd(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
