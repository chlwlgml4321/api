package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants;

public enum GcDstbTrdDivCd {

    DISTRIBUTOR_CHARGE("DC"), // 충전
    CANCEL_DISTRIBUTOR("CD"), // 충전취소
    DISTRIBUTOR_USE("DU"), // 묶음상품권 발행
    DISTRIBUTOR_DECREASE("DD"), // 선불 PIN 충전
    WITHDRAW_DISTRIBUTOR("WD"),
    ;

    private final String code;
    GcDstbTrdDivCd(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
