package kr.co.hectofinancial.mps.global.constant;

/**
 * 자동충전 충전수단 구분
 */
public enum AutoChargeMethodType {
    ACCOUNT, //계좌
    CARD; //카드

    public static AutoChargeMethodType getAutoChargeAccountType(String type) {
        if ("A".equals(type)) return ACCOUNT;
        if ("C".equals(type)) return CARD;
        return null;
    }
}
