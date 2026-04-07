package kr.co.hectofinancial.mps.global.constant;

/**
 * 선불시스템 관리자 거래 요청 enum
 * MPS.TB_MPS_ADMIN_TRD_DTL 테이블의 RSLT_STAT_CD
 */
public enum AdminRsltStatCd {
    // SUCC: 거래성공, WAIT: 거래요청, F: 거래실패
    WAITTING("WAIT"), SUCCESS("SUCC"), FAIL("FAIL");
    private final String rsltStatCd;

    AdminRsltStatCd(String rsltStatCd) {
        this.rsltStatCd = rsltStatCd;
    }

    public String getRsltStatCd() {
        return this.rsltStatCd;
    }
}

