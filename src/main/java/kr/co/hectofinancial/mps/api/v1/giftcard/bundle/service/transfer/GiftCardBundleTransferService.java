package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.transfer;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcBndlStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundlePin;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.transfer.GiftCardBundleTransferRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.transfer.GiftCardBundleTransferResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.GiftCardBundleCommonService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleTransferService {

    private final GiftCardBundleTransferServiceSupport serviceSupport;
    private final GiftCardBundleCommonService giftCardBundleCommonService;

    @Transactional
    public GiftCardBundleTransferResponseDto transfer(GiftCardBundleTransferRequestDto dto) {
        log.info("[GcBndlTrx][START] Transfer giftcard bundle");

        String dstbTrdNo = giftCardBundleCommonService.generateDistributorTradeNo();

        LocalDateTime now = DateTimeUtil.getCurrentDateTime();

        String reqDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String reqTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);

        String mReqDtm = reqDt + reqTm;
        if (StringUtils.isNotEmpty(dto.getReqDt()) && StringUtils.isNotEmpty(dto.getReqTm())) {
            mReqDtm = dto.getReqDt() + dto.getReqTm();
        }

        // 상품권 번호 암호화
        String bndlPinNoEnc = giftCardBundleCommonService.encrypt(dto.getGcBndlNo());
        String issDt = dto.getIssDt();

        try {
            /* 유통 정보 조회 (동시성 체크) */
            GiftCardBundleDistributor distributor = checkCurrency(dto);

            /* 유통 잔액 일치 확인 */
            checkDistributorBalance(distributor, dto.getDstbBlc());

            /* 묶음상품권 정보 확인 */
            GiftCardBundlePin gcBndlPin = checkGiftCardBundlePin(bndlPinNoEnc, issDt);

            /* 묶음상품권 금액 확인 */
            long trdAmt = Long.parseLong(dto.getTrdAmt());
            if (trdAmt != gcBndlPin.getBndlAmt()) {
                log.info("[GcBndlTrx][ERROR] Amount is not matched | reqAmt=[{}] | dbAmt=[{}]", trdAmt, gcBndlPin.getBndlAmt());
                throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_AMOUNT_IS_NOT_MATCHED);
            }

            /* 묶음 상품권 양도 처리 */
            updateGiftCardBundleStatus(dto, gcBndlPin);

            /* 유통 잔액 증가 */
            long dstbBlc = updateDstbBlc(dto, distributor);
            log.info("[GcBndlTrx][STEP] Update distributor balance | gcDstbNo: {} | balance: {}",
                    dto.getGcDstbNo(), dstbBlc);

            /* 유통 거래 이력 저장 */
            createHistory(dto, dstbTrdNo, reqDt, reqTm, mReqDtm, bndlPinNoEnc);
            log.info("[GcBndlTrx][STEP] insertDistributorHistory | dstbTrdNo: {}", dstbTrdNo);

            return GiftCardBundleTransferResponseDto.builder()
                    .gcDstbNo(dto.getGcDstbNo())
                    .useMid(dto.getUseMid())
                    .mTrdNo(dto.getMTrdNo())
                    .dstbTrdNo(dstbTrdNo)
                    .gcBndlNo(dto.getGcBndlNo())
                    .gcBndlStatCd(GcBndlStatCd.TRANSFER.getCode())
                    .trdAmt(dto.getTrdAmt())
                    .dstbBlc(String.valueOf(dstbBlc))
                    .procDt(reqDt)
                    .procTm(reqTm)
                    .issDt(gcBndlPin.getIssDt())
                    .build();
        } catch (RequestValidationException ex) {
            log.error("[GcBndlTrx][STEP] Fail to Transfer | message = {}", ex.getErrorCode(), ex);

            /* 유통 거래 내역 실패 저장 */
            createFailHistory(dstbTrdNo, reqDt, reqTm, mReqDtm, bndlPinNoEnc, dto, ex.getErrorCode());
            throw ex;
        } catch (Exception ex) {
            log.error("[GcBndlTrx][STEP] Fail to Transfer | message = {}", ex.getMessage(), ex);

            /* 유통 거래 내역 실패 저장 */
            createFailHistory(dstbTrdNo, reqDt, reqTm, mReqDtm, bndlPinNoEnc, dto, ErrorCode.UNDEFINED_SERVER_ERROR_CODE);

            /* MTMS */
            alarmMtms(dto, ex.getMessage(), ErrorCode.UNDEFINED_SERVER_ERROR_CODE);

            throw new RequestValidationException(ErrorCode.UNDEFINED_SERVER_ERROR_CODE);
        } finally {
            log.info("[GcBndlTrx][END] Transfer giftcard bundle");
        }
    }

    private void createFailHistory(
            String dstbTrdNo,
            String reqDt,
            String reqTm,
            String mReqDtm,
            String bndlPinNoEnc,
            GiftCardBundleTransferRequestDto dto,
            ErrorCode errorCode
    ) {
        try {
            serviceSupport.insertDstbTradeFail(
                    dstbTrdNo,
                    reqDt,
                    reqTm,
                    mReqDtm,
                    bndlPinNoEnc,
                    dto,
                    errorCode
            );
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlIss][STEP] Insert dstb_trd_fail is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(dto, "유통 거래 실패 이력 저장 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private void createHistory(GiftCardBundleTransferRequestDto dto, String dstbTrdNo, String reqDt, String reqTm, String mReqDtm, String bndlPinNoEnc) {
        try {
            serviceSupport.insertDstbTrade(
                    dstbTrdNo,
                    reqDt,
                    reqTm,
                    mReqDtm,
                    bndlPinNoEnc,
                    dto
            );
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlIss][STEP] Insert dstb_trd is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(dto, "유통 거래 이력 저장 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private long updateDstbBlc(GiftCardBundleTransferRequestDto dto, GiftCardBundleDistributor distributor) {
        long dstbBlc;
        try {
            long trdAmt = Long.parseLong(dto.getTrdAmt());
            if (distributor == null) {
                dstbBlc = serviceSupport.insertGiftCardDistributorBalance(dto.getGcDstbNo(), trdAmt);
            } else {
                dstbBlc = serviceSupport.updateGiftCardDistributorBalance(distributor, trdAmt);
            }
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlIss][STEP] Insert dstb_trd_fail is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(dto, "유통 잔액 업데이트 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
        return dstbBlc;
    }

    private void updateGiftCardBundleStatus(GiftCardBundleTransferRequestDto dto, GiftCardBundlePin gcBndlPin) {
        try {
            serviceSupport.transferGiftCardBundle(gcBndlPin);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlTrx][STEP] Update giftcard bundle is failure | message: {}", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(dto, "유통 잔액 업데이트 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private GiftCardBundlePin checkGiftCardBundlePin(String bndlPinNoEnc, String issDt) {
        GiftCardBundlePin gcBndlPin;
        try {
            /* 동시성 체크 */
            if (StringUtils.isBlank(issDt)) {
                gcBndlPin = giftCardBundleCommonService.getGiftCardBundlePinWithLock(bndlPinNoEnc);
            } else {
                gcBndlPin = giftCardBundleCommonService.getGiftCardBundlePinWithLock(bndlPinNoEnc, issDt);
            }
        } catch (CannotAcquireLockException e) {
            log.info("[GcBndlTrx][STEP] 선행처리로 인한 거절 | bndlPinNoEnc=[{}]", bndlPinNoEnc);
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
        }

        log.info("[GcBndlTrx][STEP] Gift card bundle | bndlPinNoEnc:{} | info: {}", bndlPinNoEnc, gcBndlPin);
        /* 없으면 에러 */
        if (gcBndlPin == null) {
            throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_IS_NOT_EXIST);
        }

        /* 묶음 상품권 상태 확인 */
        if (!GcBndlStatCd.ISSUE.getCode().equals(gcBndlPin.getPinStatCd())) {
            /* 사용가능이 아니면 에러 */
            log.info("[GcBndlTrx][STEP] Bundle giftcard transfer not allowed | pinStatCd: {}", gcBndlPin.getPinStatCd());
            throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_TRANSFER_NOT_ALLOWED);
        }

        /* 묶음상품권 유효기간 확인 */
        LocalDate vldPd = LocalDate.parse(gcBndlPin.getVldPd(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (LocalDate.now().isAfter(vldPd)) {
            log.info("[GcBndlTrx][STEP] Giftcard bundle is expired | vldPd: {}", vldPd);
            throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_IS_EXPIRED);
        }
        return gcBndlPin;
    }

    private GiftCardBundleDistributor checkCurrency(GiftCardBundleTransferRequestDto dto) {
        GiftCardBundleDistributor distributor;
        try {
            distributor = giftCardBundleCommonService.getDistributorWithLock(dto.getGcDstbNo());
        } catch (CannotAcquireLockException cale) {
            log.error("[GcBndlTrx][STEP] Distributor is in progress | gcDstbNo={} | msg={}", dto.getGcDstbNo(), cale.getMessage(), cale);
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
        }

        return distributor;
    }

    private void checkDistributorBalance(GiftCardBundleDistributor distributor, String reqBlc) {
        if (distributor == null) {
            return;
        }

        /* 요청 잔액과 현재 잔액이 다른 경우 */
        long reqDstbBlc = Long.parseLong(reqBlc);
        if (reqDstbBlc != distributor.getDstbBlc()) {
            log.info("[GcBndlTrx][STEP] Balance is different | req_dstbBlc: {} | db_dstbBlc: {}", reqDstbBlc, distributor.getDstbBlc());
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);
        }
    }

    private void alarmMtms(
            GiftCardBundleTransferRequestDto dto,
            String errMsg,
            ErrorCode errorCode
    ) {
        String message = String.format("**ERROR 발생** 묶음상품권 양도 / " +
                "[" +
                "M_ID: " + dto.getUseMid() +
                ", 금액: " + dto.getTrdAmt() +
                ", 유통관리번호: " + dto.getGcDstbNo() +
                ", 묶음상품권 번호: " + giftCardBundleCommonService.encrypt(dto.getGcBndlNo()) +
                ", 오류내용: " + errMsg +
                "][" + MDC.get("jsessionId") + "]"
        );
        MonitAgent.sendMonitAgent(errorCode.getErrorCode(), message);
    }
}
