package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants;

public enum DstbPrdtCd {

    GIFTCARD_USE("UPIN"), // 상품권사용
    USE("DUSE"), // 유통잔액사용
    CHARGE("CPIN") // 유통잔액충전
    ;

    private final String prdtCd;

    DstbPrdtCd(String prdtCd) {
        this.prdtCd = prdtCd;
    }

    public String getPrdtCd() {
        return prdtCd;
    }
}
