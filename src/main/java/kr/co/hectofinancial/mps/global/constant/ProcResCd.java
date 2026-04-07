package kr.co.hectofinancial.mps.global.constant;

/**
 * 원장 관련 프로시저 응답값
 */
public enum ProcResCd {

    /**
     * 0 정상처리
     * 20001 검증실패 (거래금액 0원 이하)
     * 20002 ERROR
     * 20003 잔액불일치
     * 20004 지급한도초과
     * 20005 월지급한도초과
     * 20006 요청금액불일치
     * 20007 포인트 만료금액 불일치
     * 20009 선행처리로 인한 처리 거절
     * 9999 기타 커넥션 오류
     */
    SUCCESS(0),
    RETRY_NEEDED(-20001),
    ERROR(-20002),
    BALANCE_NOT_MATCHED(-20003),
    LIMIT_REACHED(-20004),
    MONTHLY_LIMIT_REACHED(-20005),
    REQ_AMT_NOT_MATCHED(-20006),
    EXPR_POINT_AMT_NOT_MATCHED(-20008),
    LOW_LOCK_CUST_WALLET(-20009),
    ETC_FAIL(9999),
    ;

    ProcResCd(long resCd) {
        this.resCd = resCd;
    }
    private long resCd;
    public long getResCd() {
        return this.resCd;
    }

}
