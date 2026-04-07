package kr.co.hectofinancial.mps.global.constant;

/**
 * 영업일 구분 코드
 */
public enum HolidayStatCd {
    WORKING("B"),
    HOLIDAY("H"),
    SATURDAY("S");
    private final String hldStatCd;

    HolidayStatCd(String hldStatCd) {
        this.hldStatCd = hldStatCd;
    }

    public String getHldStatCd() {
        return hldStatCd;
    }
}
