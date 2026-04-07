package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.search;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcDstbTrdDivCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTrade;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleBalanceRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleBalanceResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.GiftCardBundleBalanceUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.GiftCardBundleBalanceUseResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.cancel.GiftCardBundleBalanceUseCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.cancel.GiftCardBundleBalanceUseCancelResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardDistributorTradeRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.GiftCardBundleCommonService;
import kr.co.hectofinancial.mps.api.v1.trade.repository.CustWlltRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleBalanceService {

    private final GiftCardBundleCommonService bundleCommonService;
    private final GiftCardBundleBalanceServiceSupport serviceSupport;
    private final CustWlltRepository custWlltRepository;
    private final GiftCardDistributorTradeRepository distributorTradeRepository;

    /**
     * 유통 잔액 조회
     * @param dto
     * @return
     */
    @Transactional(readOnly = true)
    public GiftCardBundleBalanceResponseDto getBalance(GiftCardBundleBalanceRequestDto dto) {
        log.info("[GiftCardBundleBalance][START] Get distributor info | gcDstbNo={}", dto.getGcDstbNo());

        try {
            /* 유통 잔액 조회 */
            GiftCardBundleDistributor distributor = bundleCommonService.getDistributor(dto.getGcDstbNo());
            log.info("[GiftCardBundleBalance][STEP] Load distributor info | exists={}", distributor != null);
            if (distributor == null) {
                return GiftCardBundleBalanceResponseDto.builder()
                        .gcDstbNo(dto.getGcDstbNo())
                        .dstbBlc("0")
                        .useMid(dto.getUseMid())
                        .lastBlcUpdtDtm(null)
                        .build();
            }

            LocalDateTime lastBlcUpdtDtm = distributor.getModifiedDate() == null ? distributor.getCreatedDate() : distributor.getModifiedDate();

            log.info("[GiftCardBundleBalance][END] Get distributor info | gcDstbNo={}", dto.getGcDstbNo());
            return GiftCardBundleBalanceResponseDto.builder()
                    .gcDstbNo(distributor.getGcDstbNo())
                    .dstbBlc(String.valueOf(distributor.getDstbBlc()))
                    .useMid(dto.getUseMid())
                    .lastBlcUpdtDtm(lastBlcUpdtDtm.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                    .build();
        } catch (Exception e) {
            log.error("유통잔액조회 실패 | msg={}", e.getMessage(), e);
            alarmMtmsWhenGetBalance(
                    dto.getGcDstbNo(),
                    dto.getUseMid(),
                    e.getMessage()
            );
            throw new RequestValidationException(ErrorCode.DISTRIBUTOR_BALANCE_SEARCH_IS_ERROR);
        }
    }

    /**
     * 유통 잔액 사용
     * @param dto
     * @return
     */
    @Transactional
    public GiftCardBundleBalanceUseResponseDto use(GiftCardBundleBalanceUseRequestDto dto) {
        /* 서버 시간 */
        LocalDateTime now = DateTimeUtil.getCurrentDateTime();

        String trdDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String trdTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);
        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(dto.getReqDt()) && StringUtils.isNotEmpty(dto.getReqTm())) {
            mReqDtm = dto.getReqDt() + dto.getReqTm();
        }

        long trdAmt = Long.parseLong(dto.getTrdAmt());
        long dstbBlc = Long.parseLong(dto.getDstbBlc());

        log.info("[GcBndlDstbBlcUse][START] GC_DSTB_NO={} | M_ID={} | TRD_AMT={}", dto.getGcDstbNo(), dto.getUseMid(), trdAmt);

        try {
            /* 요청 금액 검증 */
            validTrdAmt(trdAmt);

            log.info("[GcBndlDstbBlcUse][STEP] Valid trdAmt is success");

            /* 잔액 조회 (동시성 체크) */
            GiftCardBundleDistributor distributor = checkCurrency(dto);

            /* 잔액 검증 */
            validDstbBlc(trdAmt, dstbBlc, distributor);
            log.info("[GcBndlDstbBlcUse][STEP] Valid balance is success");

            /* 유통 잔액 사용 */
            long useBlc = useBalance(dto, trdAmt, distributor);
            log.info("[GcBndlDstbBlcUse][STEP] Update distributor balance success");

            /* 유통 거래 내역 번호 생성 */
            String dstbTrdNo = createHistory(dto, trdDt, trdTm, mReqDtm);
            log.info("[GcBndlDstbBlcUse][STEP] Create distributor trade history={}", dstbTrdNo);

            return GiftCardBundleBalanceUseResponseDto.builder()
                    .gcDstbNo(dto.getGcDstbNo())
                    .useMid(dto.getUseMid())
                    .mTrdNo(dto.getMTrdNo())
                    .dstbTrdNo(dstbTrdNo)
                    .trdAmt(dto.getTrdAmt())
                    .dstbBlc(String.valueOf(useBlc))
                    .trdDt(trdDt)
                    .trdTm(trdTm)
                    .trdSumry(dto.getTrdSumry())
                    .mResrvField1(dto.getMResrvField1())
                    .mResrvField2(dto.getMResrvField2())
                    .mResrvField3(dto.getMResrvField3())
                    .build();
        } catch (RequestValidationException ex) {
            /* 유통 거래 실패내역 저장 */
            log.error("[GcBndlDstbBlcUse][STEP] Fail to validate | code=[{}] | message=[{}]",
                    ex.getErrorCode().getErrorCode(), ex.getErrorCode().getErrorMessage(), ex);
            /* 유통 거래 내역 번호 생성 */
            createFailHistory(dto, trdDt, trdTm, mReqDtm, ex.getErrorCode());
            throw ex;
        } catch (Exception ex) {
            /* 유통 거래 실패내역 저장 */
            log.error("[GcBndlDstbBlcUse][STEP] Fail to process | message=[{}]", ex.getMessage(), ex);
            createFailHistory(dto, trdDt, trdTm, mReqDtm, ErrorCode.UNDEFINED_SERVER_ERROR_CODE);

            /* MTMS */
            alarmMtms(dto, String.valueOf(ex.getStackTrace()[0]), ErrorCode.UNDEFINED_SERVER_ERROR_CODE);
            throw new RequestValidationException(ErrorCode.UNDEFINED_SERVER_ERROR_CODE);
        } finally {
            log.info("[GcBndlDstbBlcUse][END] GC_DSTB_NO={} | M_ID={} | TRD_AMT={}", dto.getGcDstbNo(), dto.getUseMid(), trdAmt);
        }
    }

    /**
     * 유통 잔액 사용 취소
     * @param dto
     * @return
     */
    @Transactional
    public GiftCardBundleBalanceUseCancelResponseDto useCancel(GiftCardBundleBalanceUseCancelRequestDto dto) {
        log.info("[GiftCardBundleBalanceUseCancel][START] Use Balance cancel giftCard bundle");

        LocalDateTime now = DateTimeUtil.getCurrentDateTime();
        String trdDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String trdTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);
        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(dto.getReqDt()) && StringUtils.isNotEmpty(dto.getReqTm())) {
            mReqDtm = dto.getReqDt() + dto.getReqTm();
        }

        String dstbTrdNo = bundleCommonService.generateDistributorTradeNo();
        log.info("[GiftCardBundleBalanceUseCancel][STEP] Generate distributor trade number | dstbTrdNo: {}", dstbTrdNo);

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

            /* 원거래 조회 */
            GiftCardDistributorTrade gcTrd = distributorTradeRepository.findByDstbTrdNoAndTrdDt(dto.getOrgDstbTrdNo(), dto.getOrgTrdDt());
            if (gcTrd == null) {
                log.info("[GiftCardBundleBalanceUseCancel][ERROR] Not exist original dstb trade | orgTrdNo={} | orgTrdDt={}", dto.getOrgDstbTrdNo(), dto.getOrgTrdDt());
                throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_NOT_FOUND);
            }

            /* 취소 금액 확인 */
            if (gcTrd.getTrgAmt() != Long.parseLong(dto.getTrdAmt())) {
                log.info("[GiftCardBundleBalanceUseCancel][ERROR] Amount is not matched | req={} | db={}", dto.getTrdAmt(), gcTrd.getTrgAmt());
                throw new RequestValidationException(ErrorCode.REQ_AMT_NOT_MATCHED);
            }

            /* 원거래 확인 */
            validateOriginalTrade(gcTrd);

            /* 유통 잔액 충전 */
            long cnclBlc = getCnclBlc(distributor, dto);
            log.info("[GiftCardBundleChargeCancel][STEP] cancel dstb balance is success");

            /* 유통 이력 저장 */
            insertDstbTradeForCancel(dto, trdDt, trdTm, mReqDtm, dstbTrdNo);
            log.info("[GiftCardBundleChargeCancel][STEP] Insert dstb_trd is success");

            /* 원거래 취소여부 Y로 업데이트 */
            gcTrd.setCnclYn("Y");
            gcTrd.setModifiedDate(LocalDateTime.now());
            gcTrd.setModifiedId(ServerInfoConfig.HOST_NAME);
            gcTrd.setModifiedIp(ServerInfoConfig.HOST_IP);
            log.info("[GiftCardBundleChargeCancel][STEP] Update orgDstbTrd cnclYn is success");

            return GiftCardBundleBalanceUseCancelResponseDto.builder()
                    .gcDstbNo(dto.getGcDstbNo())
                    .useMid(dto.getUseMid())
                    .mTrdNo(dto.getMTrdNo())
                    .dstbTrdNo(dstbTrdNo)
                    .trdAmt(dto.getTrdAmt())
                    .dstbBlc(String.valueOf(cnclBlc))
                    .trdDt(trdDt)
                    .trdTm(trdTm)
                    .trdSumry(dto.getTrdSumry())
                    .mResrvField1(dto.getMResrvField1())
                    .mResrvField2(dto.getMResrvField2())
                    .mResrvField3(dto.getMResrvField3())
                    .build();
        } catch (RequestValidationException ex) {
            log.error("[GiftCardBundleChargeCancel][ERROR] Fail to use balance cancel | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);
            serviceSupport.insertDstbTradeFailForCancel(
                    dto,
                    dstbTrdNo,
                    trdDt,
                    trdTm,
                    mReqDtm,
                    ex.getErrorCode()
            );
            throw ex;
        } catch (Exception ex) {
            log.error("[GiftCardBundleChargeCancel][ERROR] Fail to use balance cancel | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);

            ErrorCode errorCode = ErrorCode.UNDEFINED_SERVER_ERROR_CODE;
            serviceSupport.insertDstbTradeFailForCancel(
                    dto,
                    dstbTrdNo,
                    trdDt,
                    trdTm,
                    mReqDtm,
                    errorCode
            );

            String message = String.format("**ERROR 발생** 유통잔액 사용취소 / " +
                    "[유통관리번호: " + dto.getGcDstbNo() +
                    ", 사용처 상점 아이디: " + dto.getUseMid() +
                    ", 원거래번호: " + dto.getOrgDstbTrdNo() +
                    ", 원거래일자: " + dto.getOrgTrdDt() +
                    ", 오류내용: " + ex.getMessage() +
                    "]");
            throw new RequestValidationException(ErrorCode.UNDEFINED_SERVER_ERROR_CODE, message);
        }
    }

    private void validateOriginalTrade(GiftCardDistributorTrade gcTrd) {
        String orgDstbTrdNo = gcTrd.getDstbTrdNo();
        String orgTrdDt = gcTrd.getTrdDt();

        // 선불 PIN 충전을 위한 유통잔액 사용만 취소 가능. 묶음상품권 발행은 안됨
        if (!GcDstbTrdDivCd.DISTRIBUTOR_DECREASE.getCode().equals(gcTrd.getTrdDivCd())) {
            log.info("[GiftCardBundleBalanceUseCancel][ERROR] Only TrdDivCd DD is cancel | orgTrdNo={} | orgTrdDt={}", orgDstbTrdNo, orgTrdDt);
            throw new RequestValidationException(ErrorCode.DISTRIBUTOR_USE_CANCEL_ONLY_WHEN_CHARGE_PIN);
        }

        /* 취소내역인 경우 */
        if ("Y".equals(gcTrd.getCnclYn())) {
            log.info("[GiftCardBundleBalanceUseCancel][ERROR] This is cancel trade | dstbTrdNo={} | trdDt={}", orgDstbTrdNo, orgTrdDt);
            throw new RequestValidationException(ErrorCode.NOT_VALID_TRD_DIV_CD);
        }

        /* 이미 취소 내역이 있는 경우 */
        GiftCardDistributorTrade orgGcTrd = distributorTradeRepository.findByOrgDstbTrdNoAndOrgTrdDt(orgDstbTrdNo, orgTrdDt);
        if (orgGcTrd != null) {
            log.info("[GiftCardBundleBalanceUseCancel][ERROR] Already cancelled | orgTrdNo={} | orgTrdDt={}",orgDstbTrdNo, orgTrdDt);
            throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_CANCELED);
        }
    }

    private void insertDstbTradeForCancel(GiftCardBundleBalanceUseCancelRequestDto dto, String trdDt, String trdTm, String mReqDtm, String dstbTrdNo) {
        try {
            serviceSupport.insertDstbTradeForCancel(dto, dstbTrdNo, trdDt, trdTm, mReqDtm);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.info("[GiftCardBundleChargeCancel][STEP] insertDstbTrade is failure | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);

            String message = String.format("**ERROR 발생** 유통잔액 사용취소 / " +
                    "[유통관리번호: " + dto.getGcDstbNo() +
                    ", 사용처 상점 아이디: " + dto.getUseMid() +
                    ", 원거래번호: " + dto.getOrgDstbTrdNo() +
                    ", 원거래일자: " + dto.getOrgTrdDt() +
                    ", 오류내용: 유통 거래 내역 저장 실패 " +
                    "]");
            MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private long getCnclBlc(GiftCardBundleDistributor distributor, GiftCardBundleBalanceUseCancelRequestDto dto) {
        try {
            long dstbBlc = distributor.getDstbBlc() + Long.parseLong(dto.getTrdAmt());
            return serviceSupport.updateBalance(distributor, dstbBlc);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.info("[GiftCardBundleChargeCancel][STEP] cancelDstbBalance is failure | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);

            String message = String.format("**ERROR 발생** 유통잔액 사용취소 / " +
                    "[유통관리번호: " + dto.getGcDstbNo() +
                    ", 사용처 상점 아이디: " + dto.getUseMid() +
                    ", 원거래번호: " + dto.getOrgDstbTrdNo() +
                    ", 원거래일자: " + dto.getOrgTrdDt() +
                    ", 오류내용: 유통 잔액 사용 취소 실패 " +
                    "]");
            MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
    }

    private void createFailHistory(GiftCardBundleBalanceUseRequestDto dto, String trdDt, String trdTm, String mReqDtm, ErrorCode undefinedServerErrorCode) {
        /* 유통 거래 내역 번호 생성 */
        String dstbTrdNo = bundleCommonService.generateDistributorTradeNo();

        /* 유통 거래 실패내역 저장 */
        serviceSupport.insertDstbTradeFail(
                dto,
                dstbTrdNo,
                trdDt,
                trdTm,
                mReqDtm,
                undefinedServerErrorCode
        );
    }

    private String createHistory(GiftCardBundleBalanceUseRequestDto dto, String trdDt, String trdTm, String mReqDtm) {
        String dstbTrdNo = bundleCommonService.generateDistributorTradeNo();
        try {
            /* 유통 거래 이력 생성 */
            serviceSupport.insertDstbTrade(
                    dto,
                    dstbTrdNo,
                    trdDt,
                    trdTm,
                    mReqDtm
            );
        } catch (DataAccessException | PersistenceException ex) {
            /* 유통 거래 실패내역 저장 */
            log.error("유통 거래 이력 저장 실패! | message=[{}]", ex.getMessage(), ex);

            /* MTMS */
            alarmMtms(dto, "유통 거래 이력 저장 실패", ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
        return dstbTrdNo;
    }

    private long useBalance(GiftCardBundleBalanceUseRequestDto dto, long trdAmt, GiftCardBundleDistributor distributor) {
        long useBlc;
        try {
            long dstbBlc = distributor.getDstbBlc() - trdAmt;
            useBlc = serviceSupport.updateBalance(distributor, dstbBlc);
        } catch (DataAccessException | PersistenceException ex) {
            /* 유통 거래 실패내역 저장 */
            log.error("유통 잔액 사용 DB 처리 실패! | message=[{}]", ex.getMessage(), ex);

            alarmMtms(
                    distributor.getGcDstbNo(),
                    dto.getUseMid(),
                    String.valueOf(trdAmt),
                    "유통 잔액 업데이트 실패",
                    ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE
            );
            throw new RequestValidationException(ErrorCode.DB_ERROR_GIFT_CARD_BUNDLE);
        }
        return useBlc;
    }

    private GiftCardBundleDistributor checkCurrency(GiftCardBundleBalanceUseRequestDto dto) {
        GiftCardBundleDistributor distributor;
        try {
            distributor = bundleCommonService.getDistributorWithLock(dto.getGcDstbNo());
        } catch (CannotAcquireLockException cale) {
            log.error("[GcBndlDstbBlcUse][STEP] Distributor is in progress | gcDstbNo={} | msg={}", dto.getGcDstbNo(), cale.getMessage(), cale);
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
        }

        if (distributor == null) {
            log.info("[GcBndlDstbBlcUse][STEP] Distributor is not exist");
            throw new RequestValidationException(ErrorCode.DISTRIBUTOR_INFO_IS_NOT_EXIST);
        }
        return distributor;
    }

    private void validDstbBlc(long trdAmt, long dstbBlc, GiftCardBundleDistributor distributor) {
        long dbBlc = distributor.getDstbBlc();
        if (dbBlc < trdAmt) {
            log.info("유통 잔액이 부족합니다");
            throw new RequestValidationException(ErrorCode.DISTRIBUTOR_BALANCE_INSUFFICIENT);
        }

        /* 요청한 잔액과 일치하지 않음 */
        if (dbBlc != dstbBlc) {
            log.info("잔액이 일치하지 않습니다.");
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);
        }
    }

    private void validTrdAmt(long trdAmt) {
        if (trdAmt == 0) {
            log.info("거래금액 오류입니다.(0원 이하)");
            throw new RequestValidationException(ErrorCode.TRADE_AMT_ERROR);
        }

        /* 최소 출금요청금액 확인 */
        if (trdAmt < 10) {
            log.info("요청하신 금액이 최소 결제금액 미만입니다.");
            throw new RequestValidationException(ErrorCode.TRADE_MNM_AMT_ERROR);
        }

        /* 십원미만 단위 확인 */
        if (trdAmt % 10 > 0) {
            log.info("올바른 금액을 입력해주세요.");
            throw new RequestValidationException(ErrorCode.NOT_VALID_AMT);
        }
    }

    private static void alarmMtms(
            GiftCardBundleBalanceUseRequestDto dto,
            String errMsg,
            ErrorCode errorCode
    ) {
        alarmMtms(dto.getGcDstbNo(), dto.getUseMid(), dto.getTrdAmt(), errMsg, errorCode);
    }

    private static void alarmMtms(
            String gcDstbNo,
            String useMid,
            String trdAmt,
            String errMsg,
            ErrorCode errorCode
    ) {
        String message = String.format("**ERROR 발생** 유통 잔액 사용 / " +
                "[" +
                "M_ID: " + useMid +
                ", 금액: " + trdAmt +
                ", 유통관리번호: " + gcDstbNo +
                ", 오류내용: " + errMsg +
                "][" + MDC.get("jsessionId") + "]"
        );
        MonitAgent.sendMonitAgent(errorCode.getErrorCode(), message);
    }

    private static void alarmMtmsWhenGetBalance(
            String gcDstbNo,
            String useMid,
            String errMsg
    ) {
        String message = String.format("**ERROR 발생** 유통 잔액 조회 / " +
                "[" +
                "M_ID: " + useMid +
                ", 유통관리번호: " + gcDstbNo +
                ", 오류내용: " + errMsg +
                "][" + MDC.get("jsessionId") + "]"
        );
        MonitAgent.sendMonitAgent(ErrorCode.DISTRIBUTOR_BALANCE_SEARCH_IS_ERROR.getErrorCode(), message);
    }
}
