package kr.co.hectofinancial.mps.global.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Getter
@Slf4j
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public enum ErrorCode {
    /**
     * 성공
     */
    SUCCESS("0000", "성공"),
    /**
     * 공통 A
     */
    ESSENTIAL_PARAM_EMPTY("A-001", "필수 파라미터를 확인해주세요."),
    PKT_HASH_NOT_MATCHED("A-002", "pktHash 값이 불일치 합니다."),
    ENCRYPT_MISSING("A-003", "암호화 필요한 필드를 확인해주세요."),
    DECRYPT_ERROR("A-004", "복호화 오류입니다."),
    ENCRYPT_ERROR("A-005", "암호화 오류입니다.","Y"),
    WORKINGDAY_FIND_ERROR("A-006", "WORKINGDAY_FIND_ERROR"),
    ESSENTIAL_PARAM_EMPTY_FIELD_NAME("A-007", "필수 파라미터를 확인해주세요: "),
    DATE_VALID_ERROR("A-008", "유효한 날짜/시간을 입력하세요."),
    YN_PARAMETER_ERROR("A-009", "Yn 파라미터는 Y 또는 N 값만 허용됩니다 : "),
    ERROR_CODE_NOT_FOUND("A-010", "오류코드를 찾을 수 없습니다."),
    PARAM_INVALID("A-011", "파라미터 값 오류: "),

    /**
     * 가맹점 M
     */
    MARKET_ADD_INFO_NOT_FOUND("M-001", "상점 정보를 찾을 수 없습니다.","R"),
    MARKET_STATUS_NOT_VALID("M-002", "가맹점 상태를 확인하세요.","R"),
    MARKET_CHRG_CD_NOT_FOUND("M-003", "가맹점 충전수단 코드 미등록.","R"),
    MARKET_CHRG_CD_ERROR("M-004", "가맹점 충전수단 미오픈.","R"),
    INVALID_STL_MID("M-005", "정산 상점 상태를 확인하세요.","R"),
    MPS_MARKET_NOT_FOUND("M-006", "선불 상점 정보를 찾을 수 없습니다.", "R"),
    NOT_AUTO_CHRG_MARKET("M-007", "자동충전 미사용 상점으로 해당 API 사용 불가합니다."),

    /**
     * 회원 C
     */
    CUSTOMER_NOT_FOUND("C-001", "존재하지 않는 회원입니다."),
    CUSTOMER_STATUS_NOT_VALID("C-002", "회원 상태를 확인하세요."),
    CUSTOMER_STATUS_WITHDRAW("C-003", "탈퇴된 회원이에요."),
    CUSTOMER_STATUS_WAITTING("C-004", "관리자의 추가 확인 절차가 진행 중인 회원입니다."),
    CUSTOMER_STATUS_STOP("C-005", "휴면 회원이에요."),
    CUSTOMER_STATUS_LOCK("C-006", "계정잠금 회원이에요."),
    CUSTOMER_UNINSCRIBED("C-007", "무기명회원 충전/출금/전환 제한."),
    CUSTOMER_CHRG_MEAN_NOT_FOUND("C-008", "출금 계좌가 등록되어있지 않아요."),
    RES_CUSTOMER_STATUS_NOT_VALID("C-010", "수신자 회원 상태를 확인하세요."),
    RES_CUSTOMER_CUSTOMER_UNINSCRIBED("C-011", "수신자 무기명회원 머니선물 제한."),
    WRONG_CUST_ID("C-012", "선불 회원 아이디 (상점 고객 아이디) 가 일치하지 않습니다. "),
    KYC_RENEW_NEEDED("C-013", "KYC 미이행 상태입니다. "),
    CUSTOMER_NOT_FOUND_BY_CUST_ID("C-014", "미가입 회원(탈퇴한 회원 포함) 입니다."),
    CI_NOT_MATCH("C-015","CI 값이 일치하지 않습니다."),
    BIZ_REG_NO_NOT_MATCH("C-016","사업자 등록번호값이 일치하지 않습니다."),
    REJECTED_CUSTOMER("C-017","가입 거절되었습니다. 고객센터로 문의바랍니다."),
    NOT_REGISTERED_CUSTOMER_STAT_CD("C-018","회원 상태 확인이 필요합니다. 고객센터로 문의바랍니다."),
    BLOCKED_CUSTOMER("C-019","관리자의 추가 확인이 필요합니다. 고객센터로 문의바랍니다."),

    /**
     * 지갑 W
     */
    GET_WITHDRAWAL_AMT_ERROR("W-001", "출금가능금액이 없습니다."),
    GET_BALANCE_ERROR("W-002", "잔액조회 오류입니다."),


    /**
     * 거래 T
     */
    TRADE_INFO_NOT_FOUND("T-001", "거래 번호를 확인해주세요."),
    TRADE_AMT_ERROR("T-002", "거래금액 오류입니다.(0원 이하)"),
    TRADE_DIV_CD_ERROR("T-003", "거래구분코드 오류입니다."),
    TRADE_CHRG_MEAN_CD_ERROR("T-004", "충전수단코드 오류입니다."),
    BALANCE_NOT_MATCHED("T-005", "잔액이 일치하지 않습니다."),
    CHARGE_LIMIT_REACHED("T-006", "충전 가능 금액을 초과하였습니다."),
    MONTHLY_LIMIT_REACHED("T-007", "월 충전 가능 금액을 초과하였습니다."),
    TRADE_ORIGINAL_NOT_FOUND("T-008", "원거래를 찾을 수 없습니다."),
    REQ_AMT_NOT_MATCHED("T-009", "원거래금액/요청금액을 확인하세요."),
    PIN_NOT_MATCHED("T-010", "결제 비밀번호가 일치하지 않습니다."),
    TRADE_ORIGINAL_CANCELED("T-011", "기취소된 거래입니다."),
    ORG_TRADE_INFO_NOT_FOUND("T-012", "원거래 번호를 확인해주세요."),
    POINT_VALIDITY_PERIOD_ERROR("T-013", "포인트 유효기간 오류입니다.(만료기한 < 현재일자)"),
    PAY_POINT_NOT_FOUND("T-014", "포인트 지급 거래건 조회 오류입니다."),
    WITHDRAW_AMT_ERROR("T-015", "출금 가능 잔액보다 출금 신청 금액이 큽니다."),
    REMITTANCE_ERROR("T-016", "머니 출금에 실패했습니다.  "),
    TRADE_CANCELED_FAIL("T-017", "취소가 불가능합니다."),
    WAIT_WITHDRAW_AMT_FAIL("T-018", "보유한 대기머니가 없습니다."),
    WITHDRAW_NAME_ERROR("T-019", "예금주명 불일치"),
    WITHDRAW_BIRTHDT_ERROR("T-020", "예금주 생년월일 불일치"),
    TRADE_MNM_AMT_ERROR("T-021", "요청하신 금액이 최소 결제금액 미만입니다."),
    INVALID_BLC_USE_ORD("T-022", "잔액 사용 순서 값을 확인하세요."),
    INVALID_CSRC_ISS_REQ_YN("T-023", "현금영수증 발행 요청 여부를 확인하세요."),
    WAIT_WITHDRAW_ERROR("T-024", "대기머니는 전액만 출금가능합니다."),
    BANK_INSPECTION("T-025", "은행 점검으로 인해 매일 23:50 ~ 00:20 사이에는 출금 신청이 불가능해요."),
    WITHDRAW_MONEYCANCEL_FAIL("T-026", "머니 출금 월 최대 횟수 초과."),
    CHARGE_CANCEL_FAIL("T-027", "이미 사용된 금액은 취소가 불가능합니다."),
    AMT_CANNOT_BE_NEGATIVE("T-028", "금액은 음수일 수 없습니다."),
    TERMINATE_WITHDRAW_FAIL("T-029", "해지 출금은 전액출금만 가능합니다."),
    CUST_FEE_AMT_ERROR("T-030", "고객부담수수료 오류 (0원 미만)"),
    POINT_VALIDITY_NOT_VALID("T-031", "포인트 만료일자를 입력하세요."),
    MONEY_GIFT_MARKET_NOT_MATCHED("T-032", "머니 선물은 동일한 상점 내 고객간 이용이 가능해요."),
    MONEY_GIFT_AMT_ERROR("T-033", "사용 가능 잔액보다 머니 선물 금액이 큽니다."),
    MONEY_GIFT_LIMIT_REACHED("T-034", "머니 선물대상 회원의 최대 보유 머니가 초과되었습니다."),
    MONEY_GIFT_FAIL("T-035", "[머니 선물 오류] 고객센터로 문의 바랍니다."),
    WAIT_MONEY_REMAINED("T-036", "대기머니 잔액이 존재해요."),
    POINT_VALIDITY_LIMIT_REACHED("T-037", "포인트 만료기한은 10년을 넘길 수 없어요."),
    RFD_RCPT_NOT_FOUND("T-038", "환불 접수 정보를 찾을 수 없어요."),
    TRADE_CHRG_UNIT_CD_ERROR("T-039", "충전 가능단위를 확인하세요."),
    CNCL_TRADE_AMT_NOT_MATCH("T-040", "취소요청 금액을 확인하세요."),
    RETRY_MONEY_WITHDRAW_NOT_MATCHED("T-041", "재처리 환불 대상 거래가 없어요."),
    NOT_POSSIBLE_CHARGE_CANCEL("T-042", "특정 충전수단 충전취소 불가."),
    CHRG_TRD_NO_ERROR("T-043", "충전거래번호를 입력하세요."),
    USE_CANCEL_AMT_ERROR("T-044", "올바르지 않은 취소 요청 금액"),
    MONEY_GIFT_CUSTNO_ERROR("T-045", "[머니 선물 오류] 본인에게 선물할 수 없어요."),
    WILL_WITHDRAWAL_ERROR("T-046", "머니 출금예정여부 조회 오류입니다."),
    WITHDRAW_BIZREGNO_ERROR("T-047", "출금 사업자번호 불일치"),
    NOT_VALID_TRD_DIV_CD("T-048", "취소 불가능 한 거래입니다."),
    MIN_MAX_VALID_ERROR("T-049", "한 페이지 사이즈는 10 이상 100 이하만 가능합니다."),
    NOT_VALID_BILL_KEY("T-050", "BILL KEY(빌키) 사용 상점이 아닙니다. 고객센터로 문의 바랍니다."),
    NOT_VALID_AMT("T-051", "올바른 금액을 입력해주세요."),
    POINT_EXPIRE_ERROR("T-052", "포인트 만료 프로시저 응답 에러."),
    TRADE_DIV_DTL_CD_ERROR("T-053", "등록되지 않은 거래구분상세 코드입니다."),
    POINT_REVOKE_AMT_ERROR("T-054", "보유한 포인트보다 많은 금액을 회수 할 수 없습니다."),
    POINT_EXPIRE_DATE_ERROR("T-055", "조회 일자는 오늘부터 최대 60일까지만 가능해요. 날짜를 다시 설정해주세요."),
    ADMIN_WITHDRAWAL_DIV_CD_ERROR("T-056", "관리자출금은 머니 출금/대기머니 출금만 가능합니다."),
    BLC_DIV_CD_ERROR("T-057", "잔액 구분 코드를 확인하세요."),
    ADMIN_CHARGE_ERROR("T-058", "관리자 수기 지급: "),
    ADMIN_CHARGE_LIST_DUPLICATE("T-059", "중복 데이터 존재"),
    TRANSFER_LIMIT_REACHED("T-060", "전환 후 머니가 최대보유 한도를 초과합니다."),
    TRANSFER_ERROR("T-061", "포인트->머니 전환만 가능해요."),
    TRANSFER_AMT_ERROR("T-062", "사용 가능 잔액보다 전환 금액이 큽니다."),

    /**
     * 예금통지 F
     */
    TRANSACTION_CODE_ERROR("F-001", "식별코드 에러(우리은행만 승인)"),
    TRANSACTION_BANK_CODE_ERROR("F-002", "은행코드 에러(우리은행만 승인)"),
    DEPOSITE_NOTICE_ERROR("F-003", "전문/업무 코드 오류"),
    DEPOSITE_NOTICE_DIV_CD_ERROR("F-004", "입/출금/취소 구분 코드 오류"),
    //    DEPOSITE_NOTICE_SIGN_CD_ERROR("F-005", "거래잔액부호 코드 오류"),
    DEPOSITE_NOTICE_SUMRY_NOT_FOUND("F-006", "ACNT_SUMRY NOT FOUND"),
    DEPOSITE_NOTICE_AMT_SIGN_CD_ERROR("F-007", "거래잔액부호 오류"),
    MACNT_NO_NOT_FOUND("F-008", "모계좌 조회 오류", "Y"),

    /**
     * BPO 카드
     */
    CARD_INFO_NOT_FOUND("B-001", "카드정보조회 오류"),
    CARD_APPROVAL_NO_ERROR("B-002", "승인번호는 8자리만 가능해요"),
    CARD_APPROVAL_CANCEL_FAIL("B-003", "카드승인취소 불가: "),

    /**
     * 상품권 - 공통
     */
    GIFT_CARD_NOT_FOUND("G-001", "등록되지 않은 상품권 번호입니다"),
    /**
     * 상품권 - 발행
     */
    GIFT_CARD_UNSUPPORTED_AMOUNT("G-002", "지원하지 않는 상품권 금액입니다"),
    GIFT_CARD_INVALID_REQUEST_TOTAL_COUNT("G-003", "발행요청건수가 잘못되었습니다"),
    GIFT_CARD_INVALID_REQUEST_TOTAL_AMOUNT("G-004", "발행요청금액이 잘못되었습니다"),
    GIFT_CARD_ISSUE_LIMIT_EXCEEDED("G-005", "상품권 최대 발행 가능 수량을 초과헀습니다"),
    /**
     * 상품권 - 사용
     */
    GIFT_CARD_USE_LIMIT_EXCEEDED("G-006", "상품권 최대 사용 가능 수량을 초과헀습니다"),
    GIFT_CARD_NOT_REGISTERED("G-007", "등록되지 않은 상품권 번호가 포함되어 있습니다"),
    GIFT_CARD_NOT_USABLE("G-008", "사용할 수 없는 상품권 번호가 포함되어 있습니다"),
    GIFT_CARD_USE_NOT_ALLOW_DUPLICATE("G-013", "중복 사용된 상품권 번호가 포함되어 있습니다"),
    /**
     * 상품권 - 재발행
     */
    GIFT_CARD_REISSUE_INVALID_AMOUNT("G-009", "해당 상품권 금액이 잘못되었습니다"),
    GIFT_CARD_NOT_REISSUABLE_STATE("G-010", "해당 상품권은 재발행 가능한 상태가 아닙니다"),
    /**
     * 상품권 - 사용취소
     */
    GIFT_CARD_NOT_CANCELABLE_STATE("G-011", "해당 상품권은 사용취소가 가능한 상태가 아닙니다", "N"),
    GIFT_CARD_NOT_FOUND_USE_TRANSACTION("G-012", "사용내역을 찾을 수 없습니다. 상품권번호 또는 원거래 정보를 다시 확인해주세요"),
    ERROR_GIFT_CARD_ISSUE("G-013", "선불상품권 발행에 실패했습니다"),
    ERROR_GIFT_CARD_REISSUE("G-014", "선불상품권 재발행에 실패했습니다"),
    ERROR_GIFT_CARD_USE("G-015", "선불상품권 사용에 실패했습니다"),
    ERROR_GIFT_CARD_USE_CANCEL("G-016", "선불상품권 사용취소에 실패했습니다"),
    /**
     * 묶음 상품권 - 공통
     */
    BUNDLE_GIFT_CARD_IS_NOT_EXIST("G-101", "상품권 정보를 찾을 수 없습니다"),
    BUNDLE_GIFT_CARD_TRANSFER_NOT_ALLOWED("G-102", "양도할 수 없는 상품권입니다"),
    BUNDLE_GIFT_CARD_USE_NOT_ALLOWED("G-103", "사용할 수 없는 상품권입니다"),
    DISTRIBUTOR_INFO_IS_NOT_EXIST("G-104", "잔액 정보가 없습니다"),
    DISTRIBUTOR_BALANCE_INSUFFICIENT("G-105", "잔액이 부족합니다"),
    BUNDLE_GIFT_CARD_UNSUPPORTED_AMOUNT("G-106", "지원하지 않는 권종입니다"),
    BUNDLE_GIFT_CARD_INVALID_REQUEST_TOTAL_AMOUNT("G-107", "요청금액이 잘못되었습니다"),
    BUNDLE_GIFT_CARD_IS_EXPIRED("G-108", "상품권이 만료되었습니다"),
    BUNDLE_GIFT_CARD_AMOUNT_IS_NOT_MATCHED("G-109", "상품권 금액이 일치하지 않습니다"),
    DB_ERROR_GIFT_CARD_BUNDLE("G-110", "상품권 DB 장애 발생"),
    DISTRIBUTOR_BALANCE_DO_NOT_CANCEL_MPS_MONEY("G-113", "선불 머니는 취소할 수 없습니다"),
    BUNDLE_GIFT_CARD_EXCEED_ISSUE_TOTAL_COUNT("G-114", "최대 발행 건수를 초과했습니다"),
    DISTRIBUTOR_USE_CANCEL_ONLY_WHEN_CHARGE_PIN("G-115", "잔액 사용취소가 불가능한 거래입니다."),
    DISTRIBUTOR_BALANCE_SEARCH_IS_ERROR("G-116", "유통잔액조회에 실패했습니다"),
    GIFT_CARD_BUNDLE_SEARCH_IS_ERROR("G-117", "묶음상품권 조회에 실패했습니다"),
    GIFT_CARD_BUNDLE_LIST_SEARCH_IS_ERROR("G-118", "묶음상품권 목록 조회에 실패했습니다"),
    
    /* 선불시스템 노티 */
    MPS_NOTI_FAIL("N-001", "선불시스템 노티 발송오류"),

    /**
     * 자동충전 관련 R
     * R-00X -> 가맹점의 실패노티에 사용됨
     * R-1XX -> 내부적으로 사용됨 (MNG,BATCH 에서 API 쐈을때)
     */

    AUTO_CHARGE_SYSTEM_ERROR("R-001", "내부 시스템 오류 입니다. "),
    BALANCE_IS_NOT_ENOUGH("R-002", "계좌의 잔액이 부족합니다. "),
    ACCOUNT_ERROR("R-003", "계좌 상태를 확인하세요. "),
    BANK_MAINTENANCE_HOUR("R-004", "은행 점검 시간입니다. "),
    MONEY_CHARGE_LIMIT_REACHED("R-005", "지갑 보유한도 초과입니다. "),
    AUTO_CHARGE_ACCOUNT_INFO_EMPTY("R-006", "고객의 자동충전 수단을 등록하세요. "),
    NOT_AUTOCHARGEABLE_MARKET("R-007", "상점의 자동충전 수단을 등록하세요. "),
    AUTO_CHARGE_NOT_SUPPORTED("R-008","자동충전 불가능한 고객입니다. "),

    DB_UPDATE_FAIL("R-100","[자동충전] 거래테이블(PM_MPS_TRD)에 chrgTrdNo 업데이트 실패! *수기처리 필요* "), //MTMS용
    DB_INSERT_FAIL("R-101","[자동충전] 거래실패테이블(PM_MPS_TRD_FAIL)에 INSERT 실패! "), //MTMS용
    CHARGE_MONEY_FAIL("R-102","[자동충전] 선불금 충전 실패! "), //MTMS용
    WHITELABEL_API_FAIL("R-103", "[자동충전] 화이트라벨 실패! "),//MTMS용
    AUTO_CHARGE_SYSTEM_FAIL("R-104", "[자동충전] 기타오류 실패! "),//MTMS용
    AUTO_CHARGE_SETTING_ERROR("R-105", "[자동충전] 기준금액 충전 정보에서 기준금액이 충전금액보다 큼! (설정오류) "),//MTMS용


    /**
     * 기타 오류 E
     */
    UNDEFINED_SERVER_ERROR_CODE("E-999", "요청에 실패하였습니다. 고객센터로 문의 바랍니다.","Y"),
    SERVER_ERROR_CODE("E-001", "요청에 실패하였습니다. 고객센터로 문의 바랍니다.","Y"),

    TOO_MANY_REQUESTS("E-002", "요청이 많습니다. 잠시 후 다시 시도하세요.","Y"),
    PKT_HASH_MAKING_EXCEPTION("E-003", "요청에 실패하였습니다. 고객센터로 문의 바랍니다.","Y"),
    WHITE_LABEL_CONNECTION_EXCEPTION("E-004", "요청에 실패하였습니다. 잠시 후 다시 시도하세요. "),
    DATE_FORMAT_CONVERSION_ERROR("E-005", "요청에 실패하였습니다. 고객센터로 문의 바랍니다.","Y"),
    REQUEST_DTO_CANNOT_BE_NULL("E-006", "요청에 실패하였습니다. 고객센터로 문의 바랍니다.","Y"),
    CANNOT_FIND_CORRECT_ALGORITHM("E-007", "요청에 실패하였습니다. 고객센터로 문의 바랍니다.","Y"),
    RESPONSE_DTO_ENCRYPTION_FAILED("E-008", "요청에 실패하였습니다. 고객센터로 문의 바랍니다.","Y"),
    MTMS_HEALTH_CHECK_ERROR("E-009", "MTMS HEALTH CHECK","Y"),
    NO_REFERENCE_DATA("E-010", "원장 기준정보 없음","Y"),
    NOTI_SERVICE_FAIL("E-011", "요청에 실패하였습니다. 충전/사용 알림 메일 발송 실패.","Y"),
    POINT_EXPIRE_FAIL("E-012", "[사용취소] 포인트 만료 에러","Y"),
    NOT_REGISTERED_COMM_CODE("E-013", "기준정보 없음(공통코드 확인필요)","Y"),
    LOW_LOCK_CUST_WALLET("E-014", "선행처리로 인한 처리 거절. 잠시 후 다시 시도하세요."),

    JSON_FORMAT_ERROR("E-015","파라미터의 JSON 형식이 올바른지 확인하세요. "),
    NOT_SUPPORTED_METHOD("E-016","지원하지 않는 HTTP METHOD 입니다. POST 로 요청바랍니다. "),
    NOT_SUPPORTED_CONTENT_TYPE("E-017","지원하지 않는 Content-Type 입니다. application/json 으로 요청바랍니다. "),
    NUMBER_FORMAT_ERROR("E-018", "숫자 형식이 아닙니다: "),
    WHITELABEL_ERROR("E-019", "결제 비밀번호 응답값 확인 필요 "),
    WHITELABEL_CONNECTION_FAIL("E-020", "결제 비밀번호 연결 실패 "),
    WHITELABEL_OTHER_ERROR("E-021","결제 비밀번호 오류 ")

    ;
    private HttpStatus httpStatus = HttpStatus.OK;
    private String errorCode;
    private String errorMessage;
    private String monitFlag; //해당 에러 모니터링 알람 보내야하는지 구분하는 값 Y => 해당 에러코드, 에러메세지 변환없이 그대로 MTMS 전송, R 이면 기준정보없음 에러코드로 변환 필요
    private String msg;

    ErrorCode(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    ErrorCode(String errorCode, String errorMessage, String monitFlag) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.monitFlag = monitFlag;
    }

    public static ErrorCode fromErrorMessage(String msg){
        log.info("msg : [{}]", msg);
        for(ErrorCode error : values()){
            if(error.errorMessage.equals(msg)){
                return error;
            }
        }
        return  ErrorCode.SERVER_ERROR_CODE;
    }
}
