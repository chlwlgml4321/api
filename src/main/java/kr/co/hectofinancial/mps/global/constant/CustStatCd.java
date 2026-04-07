package kr.co.hectofinancial.mps.global.constant;

/**
 * 회원상태 코드 enum
 * TB_MPS_CUST 테이블의 STAT_CD
 */
public enum CustStatCd {
    // N	정상,
    // D	서비스 해지,
    // W	대기,
    // H	휴면,
    // L	계정잠금,
    // K    KYC  미이행,
    // R    가입거절,
    // B    관리자 차단

    STANDARD("N"), WITHDRAW("D"), WAITTING("W"), STOP("H"), LOCK("L"), KYC_RENEW_NEEDED("K"), REJECTED("R"), BLCOK("B");
    private final String statCd;

    CustStatCd(String statCd) {
        this.statCd = statCd;
    }

    public String getStatCd() {
        return this.statCd;
    }

    /**
     * 파라미터로 넘긴 회원상태코드가 enum 에 선언된 값인지 검증
     * @param value
     * @return
     */
    public static boolean isValid(String value) {
        for (CustStatCd code : values()) {
            if (code.getStatCd().equals(value)) {
                return true;
            }
        }
        return false;
    }

}

