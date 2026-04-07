package kr.co.hectofinancial.mps.api.v1.giftcard.single.constants;

import java.util.Arrays;

public enum GiftCardAmountType {

    FIVE_THOUSAND("5000"),
    TEN_THOUSAND("10000"),
    THIRTY_THOUSAND("30000"),
    FIFTY_THOUSAND("50000"),
    ;
    private final String code;
    GiftCardAmountType(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

//    public static GiftCardAmountType fromCode(String code){
//        for (GiftCardAmountType gc : values()) {
//            if (gc.code.equals(code)) {
//                return gc;
//            }
//        }
//        throw new IllegalArgumentException("Unknown GiftCardAmountType code=" + code);
//    }

    public static boolean isValid(String code){
        return Arrays.stream(values()).anyMatch(c -> c.getCode().equals(code));
    }
}
