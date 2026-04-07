package kr.co.hectofinancial.mps.global.constant;

/**
 * MPS.TB_MPS_M 테이블의 AUTO_CHRG_MEAN_CD 값
 * 자동충전 충전수단 구분 값
 */
public enum AutoChargeMeanCd {
    BOTH("B"),
    NONE("N"),
    ACCOUNT("A"),
    CARD("C");
    private final String code;

    AutoChargeMeanCd(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
