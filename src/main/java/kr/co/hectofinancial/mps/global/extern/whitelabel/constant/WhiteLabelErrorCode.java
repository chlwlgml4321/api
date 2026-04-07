package kr.co.hectofinancial.mps.global.extern.whitelabel.constant;

import kr.co.hectofinancial.mps.global.error.ErrorCode;

import java.util.Arrays;
import java.util.Optional;

public enum WhiteLabelErrorCode {

    NOT_FOUND("ST01", "존재하지 않는 계좌"),
    INVALID("ST02", "유효하지 않은 계좌"),
    INSUFFICIENT_BALANCE("ST12", "출금계좌 잔액부족"),
    TRANSACTION_RESTRICTED("ST16", "출금계좌 거래제한"),
    ACCOUNT_ERROR("ST20", "계좌오류"),
    RECIPIENT_ACCOUNT_NOT_EXISTS("ST21", "수취인 계좌 없음"),
    LEGALLY_RESTRICTED("ST22", "법적제한 계좌"),
    ANONYMOUS_ACCOUNT("ST23", "비실명 계좌"),
    REPORTED_ACCOUNT("ST35", "사고계좌"),
    BANK_MAINTENANCE_HOUR("ST11", "은행점검 시간"),
    SYSTEM_ERROR("ST10", "내부 시스템 에러"),
    //    REGISTRATION_IN_PROGRESS("ST29", "계좌 등록 진행중"),
    //    TRANSACTION_UNAVAILABLE("ST19", "기타 거래불가"),
    //    AUTO_TRANSFER_TERMINATED("ST15", "자동이체 해지계좌"),
    ;

    private final String code;
    private final String message;


    WhiteLabelErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static WhiteLabelErrorCode fromCode(String code) {
        for (WhiteLabelErrorCode type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown account error code: " + code);
    }

    public static Optional<WhiteLabelErrorCode> findByCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst();
    }

    public static boolean exists(String code) {
        return Arrays.stream(values())
                .anyMatch(type -> type.code.equals(code));
    }

    public static Optional<ErrorCode> resolveWhiteLabelErrorCode(String code) {
        Optional<WhiteLabelErrorCode> whiteLabelErrorCode = findByCode(code);

        if (whiteLabelErrorCode.isPresent()) {
            WhiteLabelErrorCode errorCode = whiteLabelErrorCode.get();
            //잔액부족
            if (errorCode == WhiteLabelErrorCode.INSUFFICIENT_BALANCE) {
                return Optional.of(ErrorCode.BALANCE_IS_NOT_ENOUGH);
            }
            //계좌오류
            if (errorCode == WhiteLabelErrorCode.NOT_FOUND
                    || errorCode == WhiteLabelErrorCode.INVALID
                    || errorCode == WhiteLabelErrorCode.TRANSACTION_RESTRICTED
                    || errorCode == WhiteLabelErrorCode.ACCOUNT_ERROR
                    || errorCode == WhiteLabelErrorCode.RECIPIENT_ACCOUNT_NOT_EXISTS
                    || errorCode == WhiteLabelErrorCode.LEGALLY_RESTRICTED
                    || errorCode == WhiteLabelErrorCode.ANONYMOUS_ACCOUNT
                    || errorCode == WhiteLabelErrorCode.REPORTED_ACCOUNT) {
                return Optional.of(ErrorCode.ACCOUNT_ERROR);
            }
            //은행점검
            if (errorCode == BANK_MAINTENANCE_HOUR) {
                return Optional.of(ErrorCode.BANK_MAINTENANCE_HOUR);
            }
            //내부 시스템 오류
            if (errorCode == SYSTEM_ERROR) {
                return Optional.of(ErrorCode.AUTO_CHARGE_SYSTEM_ERROR);
            }
        }

        return Optional.empty();
    }
}
