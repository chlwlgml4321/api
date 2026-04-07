package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.charge.cancel;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.cancel.GiftCardBundleChargeCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.cancel.GiftCardBundleChargeCancelResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.cancel.GiftCardBundleChargeCancelVo;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.GiftCardBundleCommonService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleChargeCancelService {

    private final GiftCardBundleCommonService bundleCommonService;
    private final GiftCardBundleChargeCancelServiceSupport serviceSupport;

    @Transactional
    public GiftCardBundleChargeCancelResponseDto chargeCancel(GiftCardBundleChargeCancelRequestDto dto) {
        log.info("[GiftCardBundleChargeCancel][START] Charge cancel giftCard bundle");

        try {
            /* 유통 잔액 조회 (동시성 처리) */
            GiftCardBundleDistributor distributor;
            try {
                distributor = bundleCommonService.getDistributorWithLock(dto.getGcDstbNo());
            } catch (CannotAcquireLockException cale) {
                log.error("Distributor is in progress | gcDstbNo={} | msg={}", dto.getGcDstbNo(), cale.getMessage(), cale);
                throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
            }

            /* 잔액 일치 확인 */
            if (distributor.getDstbBlc() != Long.parseLong(dto.getDstbBlc())) {
                log.error("[GiftCardBundleChargeCancel][STEP] Distributor balance is not matched | req={} | db={}", dto.getDstbBlc(), distributor.getDstbBlc());
                throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);
            }

            /* 취소 금액 보다 잔액이 부족한 경우 */
            long trdAmt = Long.parseLong(dto.getTrdAmt());
            if (distributor.getDstbBlc() - trdAmt < 0) {
                log.error("[GiftCardBundleChargeCancel][STEP] Not enough balance | blc={} | amt={}", distributor.getDstbBlc(), trdAmt);
                throw new RequestValidationException(ErrorCode.DISTRIBUTOR_BALANCE_INSUFFICIENT);
            }

            /* 요청 정보 확인 */
            GiftCardBundleChargeCancelVo chargeCancelVo = serviceSupport.validateAndConvertRequestDto(dto);

            /* 유통 잔액 및 이력 처리 */
            long dstbBlc;
            try {
                dstbBlc = serviceSupport.cancelDstbBalance(chargeCancelVo, distributor);
            } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
                log.info("[GiftCardBundleChargeCancel][STEP] cancelDstbBalance is failure | " +
                                "Exception: {} | Cause: {} | Message: {}",
                        ex.getClass(), ex.getCause(), ex.getMessage(), ex);

                String message = String.format("**ERROR 발생** 유통잔액 충전취소 / " +
                        "[유통관리번호: " + chargeCancelVo.getGcDstbNo() +
                        ", 사용처 상점 아이디: " + chargeCancelVo.getMId()  +
                        ", 원거래번호: " + chargeCancelVo.getOrgDstbTrdNo() +
                        ", 원거래일자: " + chargeCancelVo.getOrgTrdDt() +
                        ", 오류내용: 유통 잔액 처리 실패 " +
                        "]");
                MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
                throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            }
            log.info("[GiftCardBundleChargeCancel][STEP] cancelDstbBalance is success");

            /* 유통 이력 저장 */
            try {
                serviceSupport.insertDstbTrade(chargeCancelVo);
            } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
                log.info("[GiftCardBundleChargeCancel][STEP] insertDstbTrade is failure | " +
                                "Exception: {} | Cause: {} | Message: {}",
                        ex.getClass(), ex.getCause(), ex.getMessage(), ex);

                String message = String.format("**ERROR 발생** 유통잔액 충전취소 / " +
                        "[유통관리번호: " + chargeCancelVo.getGcDstbNo() +
                        ", 사용처 상점 아이디: " + chargeCancelVo.getMId()  +
                        ", 원거래번호: " + chargeCancelVo.getOrgDstbTrdNo() +
                        ", 원거래일자: " + chargeCancelVo.getOrgTrdDt() +
                        ", 오류내용: 유통 거래 내역 저장 실패 " +
                        "]");
                MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
                throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            }
            log.info("[GiftCardBundleChargeCancel][STEP] Insert dstb_trd is success");

            return GiftCardBundleChargeCancelResponseDto.builder()
                    .gcDstbNo(dto.getGcDstbNo())
                    .useMid(dto.getUseMid())
                    .mTrdNo(dto.getMTrdNo())
                    .dstbTrdNo(chargeCancelVo.getDstbTrdNo())
                    .trdAmt(chargeCancelVo.getTrdAmt())
                    .dstbBlc(String.valueOf(dstbBlc))
                    .trdDt(chargeCancelVo.getTrdDt())
                    .trdTm(chargeCancelVo.getTrdTm())
                    .trdSumry(dto.getTrdSumry())
                    .mResrvField1(dto.getMResrvField1())
                    .mResrvField2(dto.getMResrvField2())
                    .mResrvField3(dto.getMResrvField3())
                    .build();
        } catch (RequestValidationException ex) {
            log.error("[GiftCardBundleChargeCancel][ERROR] Fail to charge cancel bundle giftcard | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);
            serviceSupport.insertDstbTradeFailWithCancel(dto, ex.getErrorCode());
            throw ex;
        } catch (Exception ex) {
            log.error("[GiftCardBundleChargeCancel][ERROR] Fail to charge cancel bundle giftcard | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);

            ErrorCode errorCode = ErrorCode.UNDEFINED_SERVER_ERROR_CODE;
            serviceSupport.insertDstbTradeFailWithCancel(dto, errorCode);

            String message = String.format("**ERROR 발생** 유통잔액 충전취소 / " +
                    "[유통관리번호: " + dto.getGcDstbNo() +
                    ", 사용처 상점 아이디: " + dto.getUseMid()  +
                    ", 원거래번호: " + dto.getOrgDstbTrdNo() +
                    ", 원거래일자: " + dto.getOrgTrdDt() +
                    ", 오류내용: " + ex.getMessage() +
                    "]");
            throw new RequestValidationException(ErrorCode.UNDEFINED_SERVER_ERROR_CODE, message);
        }
    }
}
