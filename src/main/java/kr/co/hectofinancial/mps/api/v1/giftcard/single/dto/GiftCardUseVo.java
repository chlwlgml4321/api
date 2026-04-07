package kr.co.hectofinancial.mps.api.v1.giftcard.single.dto;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardUseVo {

    private String encUseGcList; // 사용 상품권 번호 목록 (API 암호화)
    private List<String> gcNoEncList; // 사용 상품권 번호 목록 (DB 암호화)
    private List<GiftCardIssue> gcIssueList; // 사용 상품권 정보 (db 데이터)
    private String svcCd; // 사용처 상점 아이디 서비스 코드
    private String prdtCd; // 사용처 상점 아이디 상품 코드
    private long trdAmt; // 총 사용 금액 (db 데이터 합산)
    private String trdNo; // 상품권 사용 거래 번호
    private String trdDt; // 상품권 사용 일자
    private String trdTm; // 상품권 사용 일시
    private String useMid; // 사용처 상점 아이디
    private String stlMid; // 정산 상점 아이디
    private String reqDtm; // 요청일시
    private String mTrdNo; // 상점 거래 번호
    private String trdSumry; // 거래적요
    private String mResrvField1; // 예비필드1
    private String mResrvField2; // 예비필드2
    private String mResrvField3; // 예비필드3
    private LocalDateTime createDate; // 서버시간
    private String bndlPinNoEnc; // 묶음 상품권 번호 암호화
}
