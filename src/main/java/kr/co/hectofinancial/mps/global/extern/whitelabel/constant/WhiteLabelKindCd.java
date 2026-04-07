package kr.co.hectofinancial.mps.global.extern.whitelabel.constant;

public enum WhiteLabelKindCd {

    AUTH_WITHDRAWAL("/acntwdauth.do", "계좌출금인증", "AUTH_ACCNT"),
    PAY_CONFIRM("/pay_confirm.do", "결제승인요청", "PAY_CONFIRM"),
    ;

    private String endPoint;
    private String endPointKor;
    private String endPointEng;

    WhiteLabelKindCd(String endPoint, String endPointKor, String endPointEng) {
        this.endPoint = endPoint;
        this.endPointKor = endPointKor;
        this.endPointEng = endPointEng;
    }

    public String getEndPoint() {
        return this.endPoint;
    }

    public String getEndPointKor() {
        return this.endPointKor;
    }

    public String getEndPointEng() {
        return this.endPointEng;
    }

}
