package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.charge;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcDstbChrgMeanCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.GiftCardBundleChargeEtcRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.GiftCardBundleChargeEtcResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.GiftCardBundleChargeVo;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.GiftCardBundleCommonService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleChargeEtcService {

    private final GiftCardBundleCommonService bundleCommonService;
    private final GiftCardBundleChargeEtcServiceSupport serviceSupport;

    @Transactional
    public GiftCardBundleChargeEtcResponseDto charge(GiftCardBundleChargeEtcRequestDto dto) {
        log.info("[GcBndlChrgEtc][START] Charge giftCard bundle");

        /* 요청 정보 확인 */
        GiftCardBundleChargeVo chargeVo = serviceSupport.validateAndConvertRequestDto(dto);
        log.info("[GcBndlChrgEtc][STEP] ValidateAndConvertRequestDto is success | chargeVo: {}", chargeVo);

        try {
            /* 유통 잔액 조회 (동시성 체크) */
            GiftCardBundleDistributor distributor = checkCurrency(dto);
            log.info("[GcBndlChrgEtc][STEP] Check concurrency is success");

            /* 충전수단코드가 신용카드 또는 내통장이 아닌 경우 */
            checkChargeMeanCode(chargeVo);
            log.info("[GcBndlChrgEc][STEP] checkChargeMeanCode is success");

            /* 유통 잔액 저장 */
            long dstbBlc = chargeDstbBalance(chargeVo, distributor);
            log.info("[GcBndlChrgEtc][STEP] Upsert bndl_dstb is success");

            /* 유통 이력 저장 */
            insertDstbTrade(chargeVo);
            log.info("[GcBndlChrgEtc][STEP] Insert dstb_trd is success");

            return GiftCardBundleChargeEtcResponseDto.builder()
                    .gcDstbNo(chargeVo.getGcDstbNo()) // 유통 관리 번호
                    .useMid(dto.getUseMid())
                    .mTrdNo(chargeVo.getMTrdNo()) // 상점 거래 번호
                    .dstbTrdNo(chargeVo.getDstbTrdNo()) // 유통 거래 번호
                    .trdAmt(String.valueOf(chargeVo.getTrdAmt())) // 총 발행금액
                    .dstbBlc(String.valueOf(dstbBlc)) // 유통잔액
                    .trdDt(chargeVo.getTrdDt())
                    .trdTm(chargeVo.getTrdTm())
                    .chrgTrdNo(dto.getChrgTrdNo())
                    .chrgMeanCd(dto.getChrgMeanCd())
                    .trdSumry(chargeVo.getTrdSumry()) // 거래적요
                    .mResrvField1(chargeVo.getMResrvField1()) // 예비필드1
                    .mResrvField2(chargeVo.getMResrvField2()) // 예비필드2
                    .mResrvField3(chargeVo.getMResrvField3()) // 예비필드3
                    .build();
        } catch (RequestValidationException ex) {
            log.error("[GcBndlChrgEtc][STEP] Fail to validate | code=[{}] | message=[{}]",
                    ex.getErrorCode().getErrorCode(), ex.getErrorCode().getErrorMessage());
            /* 유통 거래 실패 내역 저장 */
            serviceSupport.insertDstbTradeFail(chargeVo, ex.getErrorCode());
            throw ex;
        } catch (Exception ex) {
            log.error("[GcBndlChrgEtc][STEP] Fail to charge bundle giftcard | message: {}", ex.getMessage(), ex);

            /* 유통 거래 실패 내역 저장 */
            ErrorCode errorCode = ErrorCode.UNDEFINED_SERVER_ERROR_CODE;
            serviceSupport.insertDstbTradeFail(chargeVo, errorCode);

            /* MTMS */
            alarmMtms(chargeVo, ex.getMessage(), errorCode);
            throw new RequestValidationException(errorCode);
        } finally {
            log.info("[GcBndlChrgEtc][END] GiftCard Bundle Charge");
        }
    }

    private void insertDstbTrade(GiftCardBundleChargeVo chargeVo) {
        try {
            serviceSupport.insertDstbTrade(chargeVo);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlChrgEtc][STEP] insertDstbTrade is failure | message: {}", ex.getMessage(), ex);
            alarmMtms(chargeVo, "유통 거래내역 저장 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private long chargeDstbBalance(GiftCardBundleChargeVo chargeVo, GiftCardBundleDistributor distributor) {
        try {
            return serviceSupport.chargeDstbBalance(chargeVo, distributor);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GcBndlChrgEtc][STEP] chargeDstbBalance is failure | message: {}", ex.getMessage(), ex);
            alarmMtms(chargeVo, "유통 잔액 업데이트 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private GiftCardBundleDistributor checkCurrency(GiftCardBundleChargeEtcRequestDto dto) {
        GiftCardBundleDistributor distributor;
        try {
            distributor = bundleCommonService.getDistributorWithLock(dto.getGcDstbNo());
        } catch (CannotAcquireLockException cale) {
            log.error("[GcBndlChrgEtc][STEP] Distributor is in progress | gcDstbNo={} | msg={}", dto.getGcDstbNo(), cale.getMessage(), cale);
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
        }

        if (distributor != null) { /* 유통 잔액 일치 확인 */
            if (Long.parseLong(dto.getDstbBlc()) != distributor.getDstbBlc()) {
                log.info("[GcBndlChrgEtc][STEP] Distributor balance is not matched | " +
                                "gcDstbNo=[{}] | reqBlc=[{}] | dbBlc=[{}]",
                        dto.getGcDstbNo(), dto.getDstbBlc(), distributor.getDstbBlc());
                throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);
            }
        }
        return distributor;
    }

    private void checkChargeMeanCode(GiftCardBundleChargeVo chargeVo) {
        if (!(GcDstbChrgMeanCd.CREDIT_CARD.getCode().equals(chargeVo.getChrgMeanCd()) ||
                GcDstbChrgMeanCd.EZ.getCode().equals(chargeVo.getChrgMeanCd()) ||
                GcDstbChrgMeanCd.MPS.getCode().equals(chargeVo.getChrgMeanCd()))
        ) {
            log.info("[GcBndlChrgEtc][STEP] Not supported chrgMeanCd | chrgMeanCd={}", chargeVo.getChrgMeanCd());
            throw new RequestValidationException(ErrorCode.TRADE_CHRG_MEAN_CD_ERROR);
        }
    }

    private static void alarmMtms(
            GiftCardBundleChargeVo chargeVo,
            String errMsg,
            ErrorCode errorCode
    ) {
        String message = String.format("**ERROR 발생** 유통 잔액 충전 / " +
                "[" +
                "M_ID: " + chargeVo.getMId() +
                ", 금액: " + chargeVo.getTrdAmt() +
                ", 유통관리번호: " + chargeVo.getGcDstbNo() +
                ", 충전수단코드: " + chargeVo.getChrgMeanCd() +
                ", 충전거래번호: " + chargeVo.getTrdNo() +
                ", 오류내용: " + errMsg +
                "][" + MDC.get("jsessionId") + "]"
        );
        MonitAgent.sendMonitAgent(errorCode.getErrorCode(), message);
    }
}
