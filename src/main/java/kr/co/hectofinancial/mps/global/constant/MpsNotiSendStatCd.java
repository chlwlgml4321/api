package kr.co.hectofinancial.mps.global.constant;

public enum MpsNotiSendStatCd {

    /**
     * MPS.TB_MPS_NOTI_SEND 테이블의 SEND_STAT_CD
     */
    SUCCESS("S"), FAIL("F");
    private final String sendStatCd;

    MpsNotiSendStatCd(String sendStatCd) {

        this.sendStatCd = sendStatCd;
    }

    public String getSendStatCd() {
        return this.sendStatCd;
    }
}

