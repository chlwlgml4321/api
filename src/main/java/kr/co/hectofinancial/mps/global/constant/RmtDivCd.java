package kr.co.hectofinancial.mps.global.constant;

public enum RmtDivCd {
    /**
     * 선불 예치금
     * 회원 출금
     */
    DEPOSITE("01"),
    WITHDRAW("02");

    private String rmtDivCd;

    private RmtDivCd(String rmtDivCd){this.rmtDivCd = rmtDivCd;}

    public String getRmtDivCd(){return this.rmtDivCd;}

}
