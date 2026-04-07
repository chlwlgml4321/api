package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants;

public enum GcBndlStatCd {

    PROCESSING("P"), // 발행 처리중
    ISSUE("I"), // 사용 가능
    USED("U"), // 사용 완료
    TRANSFER("T"), // 양도
    EXPIRE("E"), // 만료
    ;

    private final String code;
    GcBndlStatCd(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
