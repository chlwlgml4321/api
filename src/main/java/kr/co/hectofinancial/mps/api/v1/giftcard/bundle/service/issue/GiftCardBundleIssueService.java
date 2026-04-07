package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.issue;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcBndlConstants;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue.GiftCardBundleIssueRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue.GiftCardBundleIssueResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue.GiftCardBundleIssueVo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.service.GiftCardCommonService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleIssueService {

    private final GiftCardBundleIssueServiceSupport serviceSupport;
    private final GiftCardCommonService gcCommonService;

    @Transactional
    public GiftCardBundleIssueResponseDto issue(GiftCardBundleIssueRequestDto dto) {
        ErrorCode errorCode = ErrorCode.SUCCESS;

        log.info("[GcBndlIss][START] Issue giftcard bundle");

        /* 실패내역 저장을 위해 미리 셋팅 */
        GiftCardBundleIssueVo initIssueVo = serviceSupport.initIssueVo(dto);

        /* 발행 요청 정보 검증 */
        GiftCardBundleIssueVo issueVo = validateGiftCardBundleIssueDto(dto, initIssueVo);
        log.info("[GcBndlIss][STEP] Validate and convert is success");

        try {
            /* 총 요청건수 확인 */
            long totCnt = issueVo.getTotCnt();
            if (totCnt > GcBndlConstants.maxIssReqCnt) { // 권종에 관계없이 총 발행건수가 maxIssReqCnt를 초과하는 경우 오류
                log.info(ErrorCode.BUNDLE_GIFT_CARD_EXCEED_ISSUE_TOTAL_COUNT.getErrorMessage());
                throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_EXCEED_ISSUE_TOTAL_COUNT);
            }

            boolean isIssuingOnBatch = totCnt > GcBndlConstants.maxIssCntOnServer; // 1000장 초과한 경우 배치에서 생성

            /* 묶음 상품권 요청 저장 */
            String bndlPinNoEnc = insertGcBndlPin(issueVo, isIssuingOnBatch);
            issueVo.setBndlPinNoEnc(bndlPinNoEnc);
            log.info("[GcBndlIss][STEP] Issue giftcard bundle | bndlPinNoEnc: {}", bndlPinNoEnc);

            /* 1000장 이하인 경우, API 발급 */
            if (!isIssuingOnBatch) {
                /* 상품권 Bulk insert */
                long count = insertGcIssList(issueVo);
                log.info("[GcBndlIss][STEP] Issue giftcard list | bndlPinNoEnc: {} | count: {}", bndlPinNoEnc, count);
            }

            /* 유통 잔액 사용 */
            long dstbBlcRslt = useDistributorBalance(issueVo);
            log.info("[GcBndlIss][STEP] Use distributor balance | gcDstbNo: {} | balance: {}", issueVo.getDistributor().getGcDstbNo(), dstbBlcRslt);

            /* 유통 거래 이력 생성 */
            insertDstbTrade(issueVo);
            log.info("[GcBndlIss][STEP] Insert distributor trade");

            return GiftCardBundleIssueResponseDto.builder()
                    .gcDstbNo(dto.getGcDstbNo())
                    .useMid(dto.getUseMid())
                    .mTrdNo(dto.getMTrdNo())
                    .dstbTrdNo(issueVo.getDstbTrdNo())
                    .gcBndlNo(gcCommonService.decrypt(bndlPinNoEnc))
                    .issDt(issueVo.getIssDt())
                    .issTm(issueVo.getIssTm())
                    .trdAmt(dto.getTrdAmt())
                    .dstbBlc(String.valueOf(dstbBlcRslt))
                    .gcBndlStatCd(issueVo.getBndlPinStatCd())
                    .vldDt(issueVo.getVldPd())
                    .build();
        } catch (RequestValidationException ex) {
            log.error("[GcBndlIss][ERROR] Fail to buy bundle giftcard | code:{} | msg: {}",
                    ex.getErrorCode().getErrorCode(), ex.getErrorCode().getErrorMessage(), ex);

            /* 유통 거래 실패 저장 */
            insertDstbTradeFail(issueVo, ex.getErrorCode());
            throw ex;
        } catch (Exception ex) {
            log.error("[GcBndlIss][ERROR] Fail to buy bundle giftcard | msg: {}", ex.getMessage(), ex);

            errorCode = ErrorCode.UNDEFINED_SERVER_ERROR_CODE;

            /* 유통 거래 실패 저장 */
            insertDstbTradeFail(issueVo, errorCode);

            /* MTMS */
            alarmMtms(issueVo, ex.getMessage(), errorCode);
            throw new RequestValidationException(errorCode);
        } finally {
            /* 묶음 상품권 발행 이력 저장 */
            serviceSupport.insertGcBndlPinReq(issueVo, errorCode);
            log.info("[GcBndlIss][END] Issue giftcard bundle");
        }
    }

    private void insertDstbTrade(GiftCardBundleIssueVo issueVo) {
        try {
            serviceSupport.insertDstbTrade(issueVo);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlIss][STEP] Insert dstb_trd is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(issueVo, "유통 거래내역 저장 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private long useDistributorBalance(GiftCardBundleIssueVo issueVo) {
        try {
            return serviceSupport.useDistributorBalance(issueVo);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlIss][STEP] Update bndl_dstb is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(issueVo, "유통 잔액 업데이트 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private long insertGcIssList(GiftCardBundleIssueVo issueVo) {
        try {
            return serviceSupport.insertGcIssList(issueVo);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlIss][STEP] Insert gc_bndl_pin is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(issueVo, "선불상품권 생성 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private String insertGcBndlPin(GiftCardBundleIssueVo issueVo, boolean isIssuingOnBatch) {
        try {
            return serviceSupport.insertGcBndlPin(issueVo, isIssuingOnBatch);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlIss][STEP] Insert gc_bndl_pin is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(issueVo, "묶음상품권 생성 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private GiftCardBundleIssueVo validateGiftCardBundleIssueDto(
            GiftCardBundleIssueRequestDto dto,
            GiftCardBundleIssueVo initIssueVo
    ) {
        GiftCardBundleIssueVo issueVo;
        try {
            issueVo = serviceSupport.validateAndConvertRequestDto(dto, initIssueVo);
        } catch (RequestValidationException ex) {
            log.info("묶음상품권 발행 요청 검증 실패 : {}", ex.getErrorCode());

            /* 요청 실패 내역 저장 */
            serviceSupport.insertGcBndlPinReq(initIssueVo, ex.getErrorCode());

            /* 유통 거래 실패 내역 저장 (동시성만 처리. 아직 거래 시작 전이기 때문) */
            if (ex.getErrorCode().equals(ErrorCode.LOW_LOCK_CUST_WALLET)) {
                insertDstbTradeFail(initIssueVo, ex.getErrorCode());
            }
            throw ex;
        }
        return issueVo;
    }

    private void insertDstbTradeFail(GiftCardBundleIssueVo issueVo, ErrorCode errorCode) {
        try {
            serviceSupport.insertDstbTradeFail(issueVo, errorCode);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlIss][STEP] Insert dstb_trd_fail is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(issueVo, "유통 거래 실패 내역 저장 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private static void alarmMtms(
            GiftCardBundleIssueVo issueVo,
            String errMsg,
            ErrorCode errorCode
    ) {
        String message = String.format("**ERROR 발생** 묶음상품권 발행 / " +
                "[" +
                "M_ID: " + issueVo.getUseMid() +
                ", 금액: " + issueVo.getTrdAmt() +
                ", 유통관리번호: " + issueVo.getGcDstbNo() +
                ", 오류내용: " + errMsg +
                "][" + MDC.get("jsessionId") + "]"
        );
        MonitAgent.sendMonitAgent(errorCode.getErrorCode(), message);
    }
}
