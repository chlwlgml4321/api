package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
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
public class GiftCardBundleIssueVo {

    /**
     * Gift Card Bundle Issue Request Dto
     */
    private String gcDstbNo; // 유통관리번호
    private String useMid; // 사용처 상점 아이디
    private String mTrdNo; // 상점 거래 번호
    private String trdAmt; // 발행 요청 금액
    private List<GiftCardIssueInfo> issList; // 상품권 발행 요청 정보

    /**
     * PM_MPS_GC_BNDL_REQ 저장 정보
     */
    private String reqNo; // 요청 번호
    private String reqDt; // 요청 일자
    private String reqTm; // 요청 시간
    private String reqInfo; // 요청 정보
    private String mReqDtm; // 상점 요청 일시
    private String rsltCd; // 결과 코드
    private String rsltMsg; // 결과 메시지
    private LocalDateTime createDate; // 생성 일시
    private String createdId; // 생성 ID
    private String createdIp; // 생성 IP

    /**
     * PM_MPS_GC_BNDL_PIN 저장 정보
     */
    private String bndlPinNoEnc; // 묶음상품권 번호 암호화
    private String issDt; // 발행일자
    private String issTm; // 발행시간
    private String vldPd; // 유효기간
    private String bndlPinStatCd; // 묶음상품권 상태 코드

    /**
     * PM_MPS_GC_DSTB_TRD 저장 정보
     */
    private String dstbTrdNo; // 유통 거래 번호

    /**
     * 기타
     */
    private long totCnt;
    private GiftCardBundleDistributor distributor;

}
