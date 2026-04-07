package kr.co.hectofinancial.mps.global.constant;

public enum BizDivCd {
    /**
     * MPS.TB_MPS_CUST 테이블의 BIZ_DIV_CD
     */

    INDIVIDUAL("I"),//개인
    CORPORATE("C"),//법인사업자
    PERSONAL("P")//개인사업자
    ;


    private final String bizDivCd;

    BizDivCd(String bizDivCd) {
        this.bizDivCd = bizDivCd;
    }

    public String getBizDivCd() {
        return bizDivCd;
    }
}
