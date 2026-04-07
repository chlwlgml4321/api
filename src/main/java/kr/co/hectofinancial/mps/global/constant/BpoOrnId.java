package kr.co.hectofinancial.mps.global.constant;

/**
 * BPO ORN_ID
 */
public enum BpoOrnId {
    /**
     * BCC: 비씨카드
     * SSC: 삼성카드
     */

    BC_CARD("BCC"), SAMSUNG_CARD("SSC");

    private String ornId;

    private BpoOrnId(String ornId){this.ornId = ornId;}

    public String getOrnId(){return this.ornId;}
}
