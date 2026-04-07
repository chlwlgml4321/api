package kr.co.hectofinancial.mps.global.constant;

/**
 * 거래 구분 코드 enum
 * BAS.TB_CD 의 CD_GRP_ID = MPS_CHRG_MEAN_CD
 */
public enum TrdChrgMeanCd {

    /**
     * 잔액구분이 머니일 경우 ('M')
     * 'CA' = 신용카드
     * 'RA' = 등록계좌 --> 출금시에 회원계좌 조회
     * 'RP' = 화이트라벨 계좌
     * 'VA' = 가상계좌
     * 'ZOZ = 010가상계좌
     * 'MP' = 휴대폰
     * 'EZ' = 내통장
     * 'RT'= 송금
     * 'PIN' = 핀
     * 'MR' = 머니 선물 받기
     *
     *
     * 잔액구분이 포인트일 경우 ('P')
     * 'HP' =  당사 지급   (Hecto Point)
     * 'CP' =  가맹점 지급 (CPN Point)
     * 'PE' = 포인트만료

     * 'WL' = 화이트라벨 (자동충전 용)
     *
     * 잔액구분이 대기머니 일 경우 ('W')
     * 'WM' = 대기 머니
     * 
     * 마이그레이션
     * 'PM' = 포인트 마이그레이션
     *
     *  TRD_DIV_CD = "TF" 전환 일 경우
     *  'PTM' 포인트에서 머니 전환
     *
     * 거래구분코드가 포인트지급/머니지급/대기머니지급 이 아닐 경우,
     * 머니관련 (대기머니 포함) = M
     * 포인트관련 = P
     * 기타 (사용/사용취소 포함) = ALL
     */

    CREDITCARD_APPROVAL("CA"), DEPOSITE_ACCOUNT("DA"), MOBILE_PAYMENT("MP"), WITHDRAWL_ACCOUNT("WA"), RESISTERD_ACCOUNT("RA"), VIRTUAL_ACCOUNT("VA"), ZOZ("ZOZ"), EZ("EZ"), RP("RP"),
    HECTO_POINT("HP"), CPN_POINT("CP"), POINT_EXPIRE("PE"),REMITTANCE("RT"), PIN("PIN"),
    WAIT_MONEY("WM"),MONEY_RECEIVE("MR"),
    MONEY_RELATED("M"), POINT_RELATED("P"), ETC("ALL"), POINT_TO_MONEY("PTM"), POINT_MIGRATION("PM"),

    WHITELABEL("WL");
    private String chrgMeanCd;

    private TrdChrgMeanCd(String chrgMeanCd){this.chrgMeanCd = chrgMeanCd;}

    public String getChrgMeanCd(){return this.chrgMeanCd;}

}
