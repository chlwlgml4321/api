package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants;

public enum GiftCardBundleAmountType {

    TEN("10"),
    HUNDRED("100"),
    ONE_THOUSAND("1000"),
    TEN_THOUSAND("10000"),
    THIRTY_THOUSAND("30000"),
    FIFTY_THOUSAND("50000"),
    ;
    private final String code;
    GiftCardBundleAmountType(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static GiftCardBundleAmountType fromCode(String code){
        for (GiftCardBundleAmountType gc : values()) {
            if (gc.code.equals(code)) {
                return gc;
            }
        }
        return null;
    }
}
