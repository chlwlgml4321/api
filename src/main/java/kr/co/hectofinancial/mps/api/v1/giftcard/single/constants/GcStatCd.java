package kr.co.hectofinancial.mps.api.v1.giftcard.single.constants;

public enum GcStatCd {

    ISSUE("I"), // 발행 후 사용 가능
    USED("U"), // 사용 완료
    CANCEL("C"), // 사용 취소
    REISSUE("R"), // 교체
    EXPIRE("E"), // 만료
    TRANSFER("T"), // 양도 (묶음상품권 한정)
    ;

    private final String code;
    GcStatCd(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

        public static GcStatCd fromCode(String code){
        for (GcStatCd gc : values()) {
            if (gc.code.equals(code)) {
                return gc;
            }
        }
        throw new IllegalArgumentException("Unknown GcStatCd code=" + code);
    }
}
