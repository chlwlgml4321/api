package kr.co.hectofinancial.mps.global.constant;

public enum RfdRsltCnfStatCd {
    /**
     * S :성공
     * F :실패
     * R :재처리 대상
     */
    SUCCESS("S"),
    FAIL("F"),
    RETRY("R");

    private String rsltCnfStatCd;

    private RfdRsltCnfStatCd(String rsltCnfStatCd){this.rsltCnfStatCd = rsltCnfStatCd;}

    public String getRsltCnfStatCd(){return this.rsltCnfStatCd;}


}
