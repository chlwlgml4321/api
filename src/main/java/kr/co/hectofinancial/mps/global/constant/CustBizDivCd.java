package kr.co.hectofinancial.mps.global.constant;

public enum CustBizDivCd {
    /**
     * MPS.TB_MPS_M 테이블의 CUST_BIZ_DIV_CD
     */

    INDIVIDUAL("I"),//개인회원만 가입 가능
    CORPORATE("B"),//사업자만 가입 가능
    ALL("A")//모두 가입 가능
    ;


    private final String custBizDivCd;

    CustBizDivCd(String custBizDivCd) {
        this.custBizDivCd = custBizDivCd;
    }

    public String getCustBizDivCd() {
        return custBizDivCd;
    }
}
