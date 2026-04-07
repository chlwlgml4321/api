package kr.co.hectofinancial.mps.global.constant;

public enum MpsNotiTypeCd {

    /**
     * MPS.TB_MPS_NOTI_INFO 테이블의 NOTI_TYPE_CD
     */
    CUSTOMER_INFO_UPDATE("CUST_UPDT"), CARD_APPROVAL("CARD_APPR"), AUTO_CHARGE("AUTO_CHRG");
    private final String notiTypeCd;

    MpsNotiTypeCd(String notiTypeCd) {
        this.notiTypeCd = notiTypeCd;
    }

    public String getNotiTypeCd() {
        return this.notiTypeCd;
    }
}

