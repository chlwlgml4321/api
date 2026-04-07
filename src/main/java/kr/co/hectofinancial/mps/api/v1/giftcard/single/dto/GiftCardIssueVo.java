package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardIssueVo {

    private String mpsCustNo; // 선불 회원 번호
    private String trdDivCd; // 선불 거래 구분 코드
    private String blcUseOrd; // 잔액 사용 순서
    private List<GiftCardIssueInfo> reqIssInfoList; // 상품권 발행 요청 정보
    private String vldDt; // 유효기간
    private String mTrdNo; // 상점 거래 번호
    private String trdNo; // 상품권 발행거래번호 = 선불 거래번호
    private String trdDt; // 거래일자
    private String trdTm; // 거래시간
    private long trdAmt; // 총 발행 요청 금액 = 사용금액
    private long inBlc; // 현재 잔액 (머니 + 포인트)
    private long mnyBlc; // 머니 잔액
    private long pntBlc; // 포인트 잔액
    private String mReqDtm; // 요청일시
    private String mId; // 선불 고객 상점 아이디
    private String mCustId; // 선불 고객 아이디
    private String stlMid; // 정산 상점 아이디 = 선불 상품권 발행 상점 아이디
    private String useMid; // 사용처 상점 아이디 = 가맹점 상점 아이디
    private String mResrvField1; // 예비필드1
    private String mResrvField2; // 예비필드2
    private String mResrvField3; // 예비필드3
    private String trdSumry; // 거래적요
    private List<Map<String, String>> gcReqPktList; // 선불 거래 테이블에 저장할 발행 전문
    private LocalDateTime createDate; // 서버시간
}
