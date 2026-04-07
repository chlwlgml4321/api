package kr.co.hectofinancial.mps.global.constant;

/**
 * MPS.TB_MPS_M_CHRG_MAP 테이블의 CHRG_CNCL_PLC_CD
 *
 * A: 전체취소만 가능
 * P: 전체취소 + 부분취소 가능
 * N: 취소 불가
 */
public enum ChrgCnclPlcCd {

    ALL("P"),
    FULL_ONLY("A"),
    NONE("N");

    private String chrgCnclPlcCd;

    private ChrgCnclPlcCd(String chrgCnclPlcCd){this.chrgCnclPlcCd = chrgCnclPlcCd;}

    public String getChrgCnclPlcCd(){return this.chrgCnclPlcCd;}

}
