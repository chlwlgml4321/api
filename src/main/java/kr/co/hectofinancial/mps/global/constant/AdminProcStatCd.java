package kr.co.hectofinancial.mps.global.constant;

/**
 * 선불시스템 관리자 거래 요청 enum
 * TB_MPS_ADMIN_TRD_REQ 테이블의 PROC_STAT_CD
 */
public enum AdminProcStatCd {
    //P: 거래부분성공, A: 승인완료(거래요청), S: 거래성공, W: 승인요청, F: 거래실패
    P("P"), A("A"), SUCCESS("S"), WAITTING("W"), FAIL("F");
    private final String procStatCd;

    AdminProcStatCd(String procStatCd) {
        this.procStatCd = procStatCd;
    }

    public String getProcStatCd() {
        return this.procStatCd;
    }
}

