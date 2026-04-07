package kr.co.hectofinancial.mps.global.constant;

/**
 * 회원 구분 코드
 */
public enum CustDivCd {
    NAMED("1"),
    ANONYMOUS("0");
    private final String custDivCd;

    CustDivCd(String custDivCd) {
        this.custDivCd = custDivCd;
    }

    public String getCustDivCd() {
        return custDivCd;
    }
}
