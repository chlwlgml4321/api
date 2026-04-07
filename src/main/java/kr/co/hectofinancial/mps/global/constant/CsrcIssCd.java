package kr.co.hectofinancial.mps.global.constant;

public enum CsrcIssCd {
    /**
     * 발급대기: N
     * 접수실패: RF
     * 접수성공: RS
     * 발급실패: IF
     * 발급성공: Y
     * 발급취소: IC
     * 취소요청: CR
     * 취소실패: CF
     * 취소요청실패: CRF
     * 원거래취소: OC
     */
    RCPT_N("N"),
    RCPT_FAIL("RF"),
    RCPT_SUCCESS("RS"),
    ISSUE_FAIL("IF"),
    ISSUE_Y("Y"),
    ISSUE_CANCEL("IC"),
    CANCEL_REQUEST("CR"),
    CANCEL_FAIL("CF"),
    CANCEL_REQUEST_FAIL("CRF"),
    ORG_TRADE_CANCEL("OC");

    private String csrcIssStatCd;

    private CsrcIssCd(String csrcIssStatCd){this.csrcIssStatCd = csrcIssStatCd;}

    public String getCsrcIssStatCd(){return this.csrcIssStatCd;}


}
