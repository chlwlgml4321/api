package kr.co.hectofinancial.mps.global.constant;

import java.util.HashMap;
import java.util.Map;

public enum CnclTypeCd {

    /**
     * 1: 카드 재발급
     * 2: 무승인거래
     * 3: 매입 후 취소
     * 4: 무승인매입취소
     */
    CARD_REISSUE("1"), UNAPPROVAL("2"), CANCEL("3"), UNAPPROVAL_CANCEL("4");
    private final String cnclTypeCd;

    CnclTypeCd(String cnclTypeCd) {
        this.cnclTypeCd = cnclTypeCd;
    }

    public String getCnclTypeCd() {
        return this.cnclTypeCd;
    }

    private static final Map<String, String> CODE_MAP = new HashMap<>();

    static {
        CODE_MAP.put(UNAPPROVAL.getCnclTypeCd(), "NON_APPROVAL_SETTLE");
        CODE_MAP.put(CANCEL.getCnclTypeCd(), "SETTLE_CANCEL");
        CODE_MAP.put(UNAPPROVAL_CANCEL.getCnclTypeCd(), "NON_APPROVAL_SETTLE_CANCEL");
    }

    public static String getTradeType(String code) {
        return CODE_MAP.get(code);
    }

}

