package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.use;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcBndlStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundlePin;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.GiftCardBundleUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.GiftCardBundleUseResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.GiftCardBundleCommonService;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardUseVo;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleUseService {

    private final GiftCardBundleUseServiceSupport serviceSupport;
    private final GiftCardBundleCommonService bundleCommonService;

    @Transactional
    public GiftCardBundleUseResponseDto use(GiftCardBundleUseRequestDto dto) {
        log.info("[GcBndlUse][START] Use giftcard bundle");

        /* 요청정보 확인 */
        GiftCardUseVo useVo = serviceSupport.validateAndConvertRequestDto(dto);
        log.info("[GcBndlUse][STEP] Validate and convert result is success");

        try {
            /* 묶음상품권 동시성 처리 */
            GiftCardBundlePin gcBndlPin = checkCurrency(dto, useVo);

            /* 묶음상품권 상태 확인 */
            checkGiftCardBundlePin(gcBndlPin);

            /* 묶음상품권 금액 확인 */
            long trdAmt = Long.parseLong(dto.getTrdAmt());
            if (trdAmt != gcBndlPin.getBndlAmt()) {
                log.info("[GcBndlUse][STEP] Amount is not matched | reqAmt=[{}] | dbAmt=[{}]", trdAmt, gcBndlPin.getBndlAmt());
                throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_AMOUNT_IS_NOT_MATCHED);
            }

            /* 묶음 상품권 사용 처리 */
            updateGiftCardBundleUse(useVo, gcBndlPin);
            log.info("[GcBndlUse][STEP] Update giftcard bundle status 'USED' | bndlPinNoEnc: {} ", useVo.getBndlPinNoEnc());

            /* 묶음 상품권 사용 거래내역 저장 */
            insertGiftCardTrade(useVo);
            log.info("[GcBndlUse][STEP] Insert giftcard trade success | bndlPinNoEnc: {} ", useVo.getBndlPinNoEnc());

            /* 선불상품권 사용 이력 저장 */
            insertGiftCardUseHistory(
                    useVo.getTrdNo(),
                    useVo.getTrdDt(),
                    useVo.getBndlPinNoEnc(),
                    gcBndlPin.getIssDt(),
                    useVo
            );

            return GiftCardBundleUseResponseDto.builder()
                    .mTrdNo(useVo.getMTrdNo()) // 상점 거래 번호
                    .gcTrdNo(useVo.getTrdNo()) // 사용 거래 번호
                    .useMid(useVo.getUseMid()) // 사용처 상점 아이디
                    .gcBndlNo(dto.getGcBndlNo()) // 묶음 상품권 번호
                    .gcBndlStatCd(GcBndlStatCd.USED.getCode()) // 묶음 상품권 상태 (사용완료)
                    .trdDt(useVo.getTrdDt()) // 사용 일자
                    .trdTm(useVo.getTrdTm()) // 사용 시간
                    .trdAmt(dto.getTrdAmt())
                    .stlMid(dto.getStlMid())
                    .issDt(gcBndlPin.getIssDt())
                    .trdSumry(useVo.getTrdSumry()) // 거래 적요
                    .mResrvField1(useVo.getMResrvField1()) // 예비필드1
                    .mResrvField2(useVo.getMResrvField2()) // 예비필드2
                    .mResrvField3(useVo.getMResrvField3()) // 예비필드3
                    .build();
        } catch (RequestValidationException ex) {
            log.error("[GcBndlUse][STEP] Fail to use giftcard bundle | message: {}", ex.getMessage(), ex);

            /* 상품권 거래내역 실패 저장 */
            serviceSupport.insertGiftCardTradeFail(useVo, ex.getErrorCode());
            throw ex;
        } catch (Exception ex) {
            log.error("[GcBndlUse][STEP] Fail to use giftcard bundle | message: {}", ex.getMessage(), ex);

            /* 상품권 거래내역 실패 저장 */
            serviceSupport.insertGiftCardTradeFail(useVo, ErrorCode.UNDEFINED_SERVER_ERROR_CODE);

            /* MTMS */
            alarmMtms(useVo, ex.getMessage(), ErrorCode.UNDEFINED_SERVER_ERROR_CODE);
            throw new RequestValidationException(ErrorCode.UNDEFINED_SERVER_ERROR_CODE);
        } finally {
            log.info("[GcBndlUse][END] Use giftcard bundle");
        }
    }

    private void insertGiftCardUseHistory(
            String trdNo,
            String trdDt,
            String bndlPinNoEnc,
            String bndlPinIssDt,
            GiftCardUseVo useVo
    ) {
        try {
            serviceSupport.insertGiftCardUseHistory(
                    trdNo,
                    trdDt,
                    bndlPinNoEnc,
                    bndlPinIssDt
            );
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlUse][STEP] insertGiftCardUseHistory is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(useVo, "선불상품권 사용 이력 저장 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private void insertGiftCardTrade(GiftCardUseVo useVo) {
        try {
            serviceSupport.insertGiftCardTradeSuccess(useVo);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlUse][STEP] insertGiftCardTrade is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(useVo, "선불상품권 사용 거래 저장 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private void updateGiftCardBundleUse(GiftCardUseVo useVo, GiftCardBundlePin gcBndlPin) {
        try {
            serviceSupport.updateGiftCardBundleUse(gcBndlPin);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlUse][STEP] updateGiftCardBundleUse is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(useVo, "묶음상품권 사용 업데이트 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private GiftCardBundlePin checkCurrency(GiftCardBundleUseRequestDto dto, GiftCardUseVo useVo) {
        GiftCardBundlePin gcBndlPin;
        try { /* 동시성 처리 */
            if (StringUtils.isBlank(dto.getIssDt())) {
                gcBndlPin = bundleCommonService.getGiftCardBundlePinWithLock(useVo.getBndlPinNoEnc());
            } else {
                gcBndlPin = bundleCommonService.getGiftCardBundlePinWithLock(useVo.getBndlPinNoEnc(), dto.getIssDt());
            }
        } catch (CannotAcquireLockException cale) {
            log.error("[GcBndlUse][STEP] Gift card bundle use is in progress. msg={}", cale.getMessage(), cale);
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
        }
        return gcBndlPin;
    }

    private void checkGiftCardBundlePin(GiftCardBundlePin gcBndlPin) {
        if (gcBndlPin == null) {
            throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_IS_NOT_EXIST);
        }

        /* 묶음 상품권 상태 확인 */
        if (!GcBndlStatCd.ISSUE.getCode().equals(gcBndlPin.getPinStatCd())) {
            /* 사용가능이 아니면 에러 */
            log.info("[GcBndlUse][STEP] Bundle giftcard use not allowed | pinStatCd: {}", gcBndlPin.getPinStatCd());
            throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_USE_NOT_ALLOWED);
        }

        /* 묶음상품권 유효기간 확인 */
        LocalDate vldPd = LocalDate.parse(gcBndlPin.getVldPd(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (LocalDate.now().isAfter(vldPd)) {
            log.info("[GcBndlUse][STEP] Giftcard bundle is expired | vldPd: {}", vldPd);
            throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_IS_EXPIRED);
        }
    }

    private void alarmMtms(
            GiftCardUseVo useVo,
            String errMsg,
            ErrorCode errorCode
    ) {
        String message = String.format("**ERROR 발생** 묶음상품권 사용 / " +
                "[" +
                "M_ID: " + useVo.getUseMid() +
                ", 금액: " + useVo.getTrdAmt() +
                ", 묶음상품권 번호: " + useVo.getBndlPinNoEnc() +
                ", 오류내용: " + errMsg +
                "][" + MDC.get("jsessionId") + "]"
        );
        MonitAgent.sendMonitAgent(errorCode.getErrorCode(), message);
    }
}
