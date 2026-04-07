package kr.co.hectofinancial.mps.global.constant;

import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 거래 구분 코드 enum
 * BAS.TB_CD 의 CD_GRP_ID = MPS_TRD_DIV_CD
 */
public enum TrdDivCd {
    /**
     * CU: 공통 사용
     * MC: 머니 충전 취소
     * MP: 머니 충전
     * MW: 머니 출금
     * PC: 포인트 지급 취소
     * PE: 포인트 만료
     * PP: 포인트 지급
     * UC: 공통 사용 취소
     * WP: 대기머니 발생
     * WW: 대기머니 출금
     * TW: 해지 출금
     * MG: 머니 선물
     * MR: 머니 선물 받기
     * RP: 리또 충전
     * PR: 포인트 회수
     * AW: 관리자 출금
     * TF: 전환 (포인트->머니)
     */

    COMMON_USE("CU"), MONEY_CANCEL("MC"), MONEY_PROVIDE("MP"), MONEY_WITHDRAW("MW"), POINT_CANCEL("PC"), POINT_EXPIRE("PE"),
    POINT_PROVIDE("PP"), USE_COMMON_CANCEL("UC"), WAITMONEY_PROVIDE("WP"), WAITMONEY_WITHDRAW("WW"), TERMINATE_WITHDRAW("TW"),
    MONEY_GIFT("MG"), MONEY_RECEIVE("MR"), POINT_REVOKE("PR"), ADMIN_WITHDRAW("AW"), TRANSFER("TF"), GIFT_CARD_ISSUE("IS"),
    GIFT_CARD_BUNDLE_DISTRIBUTOR("DS"),;

    private String trdDivCd;

    private TrdDivCd(String trdDivCd){this.trdDivCd = trdDivCd;}

    public String getTrdDivCd(){return this.trdDivCd;}

    /**
     * 파라미터로 넘긴 거래구분코드가 enum 에 선언된 값인지 검증
     * @param value
     * @return
     */
    public static boolean isValid(String value) {
        for (TrdDivCd code : values()) {
            if (code.getTrdDivCd().equals(value)) {
                return true;
            }
        }
        throw new RequestValidationException(ErrorCode.TRADE_DIV_CD_ERROR);
    }

    /**
     * "취소" 들어가는 거래건 제외한 거래구분 코드 list로 return
     * @return
     */
    public static List<String> getNonCancelCodes() {
        return Arrays.stream(values())
                .filter(code -> !code.name().contains("CANCEL"))
                .map(TrdDivCd::getTrdDivCd)
                .collect(Collectors.toList());
    }

    /**
     * 거래구분 코드가 "출금" 인지 확인하는 메서드
     * @param value
     * @return
     */
    public static boolean isWithdrawCode(String value) {
        return Arrays.stream(values())
                .anyMatch(code -> (code.getTrdDivCd().equals(value)) && code.name().contains("WITHDRAW"));
    }
}
