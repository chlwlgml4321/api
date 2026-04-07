package kr.co.hectofinancial.mps.global.constant;

public enum WdTypeCd {
    /**
     * 실시간: 01
     * 배치: 02
     * 자체출금: 03
     */
    REAL_TIME("01"),
    BATCH("02"),
    SELF("03");

    private String wdTypeCd;

    private WdTypeCd(String wdTypeCd){this.wdTypeCd = wdTypeCd;}

    public String getWdTypeCd(){return this.wdTypeCd;}


}
