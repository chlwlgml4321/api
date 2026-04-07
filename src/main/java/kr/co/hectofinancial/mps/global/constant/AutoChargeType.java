package kr.co.hectofinancial.mps.global.constant;

/**
 * 자동충전 타입 구분
 */
public enum AutoChargeType {
    SHORTAGE("SH"), //부족금액충전
    THRESHOLD("ST"), //기준금액충전
    REGULAR_DAY_OF_WEEK("DW"), //정기충전(요일)
    REGULAR_DATE("DT"); //정기충전(날짜)
    private String value;

    AutoChargeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static AutoChargeType getAutoChargeType (String type) {
        if ("SH".equals(type)) return SHORTAGE;
        if ("ST".equals(type)) return THRESHOLD;
        if ("DW".equals(type)) return REGULAR_DAY_OF_WEEK;
        if ("DT".equals(type)) return REGULAR_DATE;
        return null;
    }
}
