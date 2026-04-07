package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.issue;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.DstbPrdtCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcBndlStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcDstbTrdDivCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GiftCardBundleAmountType;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.*;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue.GiftCardBundleIssueRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue.GiftCardBundleIssueVo;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.*;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.GiftCardBundleCommonService;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.service.GiftCardCommonService;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.MpsApiCd;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import kr.co.hectofinancial.mps.global.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleIssueServiceSupport {

    private final GiftCardBundleCommonService bundleCommonService;
    private final GiftCardBundleDistributorRepository distributorRepository;
    private final GiftCardDistributorTradeRepository distributorTradeRepository;
    private final GiftCardDistributorTradeFailRepository distributorTradeFailRepository;
    private final GiftCardBundlePinRepository bundlePinRepository;
    private final GiftCardBundleRequestRepository requestRepository;
    private final JsonUtil jsonUtil;
    private final GiftCardCommonService gcCommonService;

    @PersistenceContext
    private EntityManager entityManager;

    private final JdbcTemplate jdbcTemplate;

    public GiftCardBundleIssueVo initIssueVo(GiftCardBundleIssueRequestDto dto) {
        /* 발행 정보 확인 */
        if (dto.getIssList() == null || dto.getIssList().isEmpty()) {
            throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, "발행 권종 정보");
        }

        return getIssueVo(dto);
    }

    public GiftCardBundleIssueVo validateAndConvertRequestDto(
            GiftCardBundleIssueRequestDto dto,
            GiftCardBundleIssueVo issueVo
    ) {
        /* 발행 요청 금액 확인 */
        long trdAmt = getTrdAmt(dto);

        /* 유통 잔액 조회 (동시성 처리) */
        GiftCardBundleDistributor distributor;
        try { /* 동시성 처리 */
            distributor = bundleCommonService.getDistributorWithLock(dto.getGcDstbNo());
        } catch (CannotAcquireLockException cale) {
            log.error("Distributor is in progress | gcDstbNo={} | msg={}", dto.getGcDstbNo(), cale.getMessage(), cale);
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
        }

        if (distributor == null) { /* 잔액 정보가 없으면 에러 */
            log.info("No distributor info. gcDstbNo=[{}]", dto.getGcDstbNo());
            throw new RequestValidationException(ErrorCode.DISTRIBUTOR_INFO_IS_NOT_EXIST);
        }
        log.info("Distributor info=[{}]", distributor);

        /* 요청 잔액과 현재 잔액이 다른 경우 */
        long dstbBlc = Long.parseLong(dto.getDstbBlc());
        if (dstbBlc != distributor.getDstbBlc()) {
            log.info("Balance is different. req_dstbBlc=[{}], db_dstbBlc=[{}]", dstbBlc, distributor.getDstbBlc());
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);
        }

        /* 요청금액보다 잔액이 작은 경우 */
        if (distributor.getDstbBlc() < trdAmt) {
            log.info("Distributor balance insufficient");
            throw new RequestValidationException(ErrorCode.DISTRIBUTOR_BALANCE_INSUFFICIENT);
        }

        /* 권종 및 요청 금액 확인 */
        long totCnt = 0;
        long totAmt = 0;
        for (GiftCardIssueInfo gcIss : dto.getIssList()) {
            //상품권 권종 및 금액 유효성 검사
            if (StringUtils.isBlank(gcIss.getGcAmt())) {
                throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, "발행 권종 금액");
            }
            if (StringUtils.isBlank(gcIss.getGcQty())) {
                throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, "발행 권종 수량");
            }

            if (GiftCardBundleAmountType.fromCode(gcIss.getGcAmt()) == null) {
                log.info("Unsupported amount. gcAmt=[{}]", gcIss.getGcAmt());
                throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_UNSUPPORTED_AMOUNT);
            }

            try {
                totCnt += Long.parseLong(gcIss.getGcQty());
                totAmt += Long.parseLong(gcIss.getGcAmt()) * Long.parseLong(gcIss.getGcQty());
            } catch (NumberFormatException ex) {
                log.info("Invalid number format. gcAmt=[{}]", gcIss.getGcAmt());
                throw new RequestValidationException(ErrorCode.NUMBER_FORMAT_ERROR);
            }
        }

        /* 요청 금액과 권종 합산 금액이 다른 경우 */
        if (totAmt != trdAmt) {
            log.info("Gift card bundle issue is in progress");
            throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_INVALID_REQUEST_TOTAL_AMOUNT);
        }

        issueVo.setTotCnt(totCnt);
        issueVo.setDistributor(distributor);
        return issueVo;
    }

    @Transactional
    public long useDistributorBalance(GiftCardBundleIssueVo issueVo) {
        GiftCardBundleDistributor distributor = issueVo.getDistributor();
        long dstbBlc = distributor.getDstbBlc() - Long.parseLong(issueVo.getTrdAmt());

        /* 잔액 사용 */
        distributor.setDstbBlc(dstbBlc);
        distributor.setModifiedDate(issueVo.getCreateDate());
        distributor.setModifiedId(issueVo.getCreatedId());
        distributor.setModifiedIp(issueVo.getCreatedIp());
        distributorRepository.save(distributor);

        // for error logging
        entityManager.flush();

        return dstbBlc;
    }

    @Transactional
    public void insertDstbTrade(GiftCardBundleIssueVo issueVo) {
        /* 유통 거래 이력 저장 */
        GiftCardDistributorTrade trade = GiftCardDistributorTrade.builder()
                .dstbTrdNo(issueVo.getDstbTrdNo())
                .trdDt(issueVo.getIssDt())
                .trdTm(issueVo.getIssTm())
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.USE.getPrdtCd())
                .cnclYn("N")
                .mId(issueVo.getUseMid())
                .mTrdNo(issueVo.getMTrdNo())
                .mReqDtm(issueVo.getIssDt() + issueVo.getIssTm())
                .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                .trdDivCd(GcDstbTrdDivCd.DISTRIBUTOR_USE.getCode())
                .gcDstbNo(issueVo.getGcDstbNo())
                .bndlPinNoEnc(issueVo.getBndlPinNoEnc())
                .amtSign(-1)
                .trgAmt(Long.parseLong(issueVo.getTrdAmt()))
                .createdDate(issueVo.getCreateDate())
                .createdId(issueVo.getCreatedId())
                .createdIp(issueVo.getCreatedIp())
                .build();
        distributorTradeRepository.save(trade);

        // for error logging
        entityManager.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertDstbTradeFail(GiftCardBundleIssueVo issueVo, ErrorCode errorCode) {
        GiftCardDistributorTradeFail tradeFail = GiftCardDistributorTradeFail.builder()
                .dstbTrdNo(issueVo.getDstbTrdNo())
                .failDt(issueVo.getReqDt())
                .failTm(issueVo.getReqTm())
                .mId(issueVo.getUseMid())
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.USE.getPrdtCd())
                .cnclYn("N")
                .mTrdNo(issueVo.getMTrdNo())
                .mReqDtm(issueVo.getMReqDtm())
                .trdDivCd(GcDstbTrdDivCd.DISTRIBUTOR_USE.getCode())
                .amtSign(-1)
                .trgAmt(Long.parseLong(issueVo.getTrdAmt()))
                .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                .gcDstbNo(issueVo.getGcDstbNo())
                .errCd(errorCode.getErrorCode())
                .errMsg(errorCode.getErrorMessage())
                .createdDate(issueVo.getCreateDate())
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();

        distributorTradeFailRepository.save(tradeFail);
        entityManager.flush();
    }

    @Transactional
    public String insertGcBndlPin(GiftCardBundleIssueVo issueVo, boolean isIssuingOnBatch) {
        String bndlPinNo = bundleCommonService.generateBndlPinNo(issueVo.getUseMid());
        String bndlPinNoEnc = bundleCommonService.encrypt(bndlPinNo);
        String bndlPinNoMsk = bundleCommonService.maskingBndlPinNo(bndlPinNo);

        String bndlReqDtm = issueVo.getCreateDate().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String bndlCmpltDtm = isIssuingOnBatch ? null : bndlReqDtm;
        String pinStatCd = GcBndlStatCd.ISSUE.getCode();
        String gcStatCd = isIssuingOnBatch ? GcBndlStatCd.PROCESSING.getCode() : GcBndlStatCd.ISSUE.getCode();

        GiftCardBundlePin gcBndlPin = GiftCardBundlePin.builder()
                .bndlPinNoEnc(bndlPinNoEnc)
                .issDt(issueVo.getIssDt())
                .bndlPinNoMsk(bndlPinNoMsk)
                .mId(issueVo.getUseMid())
                .gcDstbNo(issueVo.getGcDstbNo())
                .bndlAmt(Long.parseLong(issueVo.getTrdAmt()))
                .bndlReqDtm(bndlReqDtm)
                .bndlCmpltDtm(bndlCmpltDtm)
                .vldPd(issueVo.getVldPd())
                .pinStatCd(pinStatCd)
                .gcStatCd(gcStatCd)
                .bndlReqInfo(issueVo.getReqInfo())
                .bndlReqNo(issueVo.getReqNo())
                .bndlReqDt(issueVo.getReqDt())
                .createdDate(issueVo.getCreateDate())
                .createdId(issueVo.getCreatedId())
                .createdIp(issueVo.getCreatedIp())
                .build();

        bundlePinRepository.save(gcBndlPin);

        // for error logging
        entityManager.flush();

        return bndlPinNoEnc;
    }

    @Transactional
    public long insertGcIssList(GiftCardBundleIssueVo issueVo) {
        List<GiftCardIssue> gcIssList = new ArrayList<>();
        for (GiftCardIssueInfo gcIss : issueVo.getIssList()) {
            int gcQty = Integer.parseInt(gcIss.getGcQty());
            long gcAmt = Long.parseLong(gcIss.getGcAmt());
            List<String> gcNoList = gcCommonService.generateGiftCardNoList(issueVo.getUseMid(), gcQty);

            /* 각 권종별 상품권 정보 생성 */
            for (String gcNo : gcNoList) {
                String gcNoMsk = gcCommonService.maskingGiftCardNo(gcNo);
                String gcNoEnc = gcCommonService.encrypt(gcNo);

                GiftCardIssue issue = GiftCardIssue.builder()
                        .bndlPinNoEnc(issueVo.getBndlPinNoEnc())
                        .bndlPinIssDt(issueVo.getIssDt())
                        .gcNoEnc(gcNoEnc)
                        .issDt(issueVo.getIssDt())
                        .gcNoMsk(gcNoMsk)
                        .issAmt(gcAmt)
                        .blc(gcAmt)
                        .vldPd(issueVo.getVldPd())
                        .useMid(issueVo.getUseMid())
                        .gcStatCd(GcBndlStatCd.ISSUE.getCode())
                        .createdDate(issueVo.getCreateDate())
                        .createdIp(ServerInfoConfig.HOST_IP)
                        .createdId(ServerInfoConfig.HOST_NAME)
                        .build();

                gcIssList.add(issue);
            }
        }

        String sql = "INSERT INTO MPS.PM_MPS_GC_ISS (" +
                "GC_NO_ENC, " +
                "ISS_DT, " +
                "GC_NO_MSK, " +
                "ISS_TRD_NO, " +
                "ISS_AMT, " +
                "BLC, " +
                "VLD_PD, " +
                "USE_M_ID, " +
                "GC_STAT_CD, " +
                "INST_DATE, " +
                "INST_ID, " +
                "INST_IP, " +
                "BNDL_PIN_NO_ENC, " +
                "BNDL_PIN_ISS_DT " +
                ") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                GiftCardIssue gcIssue = gcIssList.get(i);
                ps.setString(1, gcIssue.getGcNoEnc());
                ps.setString(2, gcIssue.getIssDt());
                ps.setString(3, gcIssue.getGcNoMsk());
                ps.setString(4, gcIssue.getIssTrdNo());
                ps.setLong(5, gcIssue.getIssAmt());
                ps.setLong(6, gcIssue.getBlc());
                ps.setString(7, gcIssue.getVldPd());
                ps.setString(8, gcIssue.getUseMid());
                ps.setString(9, gcIssue.getGcStatCd());
                ps.setObject(10, gcIssue.getCreatedDate());
                ps.setString(11, gcIssue.getCreatedId());
                ps.setString(12, gcIssue.getCreatedIp());
                ps.setString(13, gcIssue.getBndlPinNoEnc());
                ps.setString(14, gcIssue.getBndlPinIssDt());
            }

            @Override
            public int getBatchSize() {
                return gcIssList.size();
            }
        });

        // for error logging
        entityManager.flush();

        return gcIssList.size();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertGcBndlPinReq(GiftCardBundleIssueVo issueVo, ErrorCode errorCode) {
        GiftCardBundleRequest request = GiftCardBundleRequest.builder()
                .reqNo(issueVo.getReqNo())
                .reqDt(issueVo.getReqDt())
                .reqTm(issueVo.getReqTm())
                .mId(issueVo.getUseMid())
                .gcDstbNo(issueVo.getGcDstbNo())
                .reqAmt(issueVo.getTrdAmt())
                .reqInfo(issueVo.getReqInfo())
                .mReqDtm(issueVo.getMReqDtm())
                .mTrdNo(issueVo.getMTrdNo())
                .rsltCd(errorCode.getErrorCode())
                .rsltMsg(errorCode.getErrorMessage())
                .createdDate(issueVo.getCreateDate())
                .createdId(issueVo.getCreatedId())
                .createdIp(issueVo.getCreatedIp())
                .build();

        requestRepository.save(request);
    }

    private GiftCardBundleIssueVo getIssueVo(GiftCardBundleIssueRequestDto dto) {
        LocalDateTime now = DateTimeUtil.getCurrentDateTime();

        String reqDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String reqTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);

        String mReqDtm = reqDt + reqTm;
        if (StringUtils.isNotEmpty(dto.getReqDt()) && StringUtils.isNotEmpty(dto.getReqTm())) {
            mReqDtm = dto.getReqDt() + dto.getReqTm();
        }

        /* 유효기간 */
        String vldPd = LocalDate.now().plusYears(5).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return GiftCardBundleIssueVo.builder()
                // DTO
                .gcDstbNo(dto.getGcDstbNo())
                .useMid(dto.getUseMid())
                .mTrdNo(dto.getMTrdNo())
                .trdAmt(dto.getTrdAmt())
                .issList(dto.getIssList())
                // PM_MPS_GC_BNDL_REQ
                .reqNo(gcCommonService.generateTradeNo())
                .reqDt(reqDt)
                .reqTm(reqTm)
                .mReqDtm(mReqDtm)
                .reqInfo(jsonUtil.toJson(dto.getIssList()))
                .createdIp(ServerInfoConfig.HOST_IP)
                .createdId(ServerInfoConfig.HOST_NAME)
                .createDate(now)
                // PM_MPS_GC_BNDL_PIN
                .issDt(reqDt)
                .issTm(reqTm)
                .vldPd(vldPd)
                .bndlPinStatCd(GcBndlStatCd.ISSUE.getCode())
                // PM_MPS_GC_DSTB_TRD
                .dstbTrdNo(bundleCommonService.generateDistributorTradeNo())
                .build();
    }

    private long getTrdAmt(GiftCardBundleIssueRequestDto dto) {
        long trdAmt;
        try {
            trdAmt = Long.parseLong(dto.getTrdAmt());
        } catch (NumberFormatException ex) {
            throw new RequestValidationException(ErrorCode.NUMBER_FORMAT_ERROR);
        }

        /* 요청금액이 마이너스인지 확인 */
        if (trdAmt < 0) {
            log.info("Amount cannot be negative. trdAmt=[{}]", dto.getTrdAmt());
            throw new RequestValidationException(ErrorCode.AMT_CANNOT_BE_NEGATIVE);
        }

        return trdAmt;
    }
}
