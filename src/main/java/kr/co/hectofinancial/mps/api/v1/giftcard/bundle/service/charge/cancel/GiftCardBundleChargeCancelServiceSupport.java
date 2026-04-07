package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.charge.cancel;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.DstbPrdtCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcDstbTrdDivCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTrade;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTradeFail;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.cancel.GiftCardBundleChargeCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.cancel.GiftCardBundleChargeCancelVo;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardDistributorTradeFailRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardDistributorTradeRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.GiftCardBundleCommonService;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.MpsApiCd;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleChargeCancelServiceSupport {

    private final GiftCardDistributorTradeRepository distributorTradeRepository;
    private final GiftCardDistributorTradeFailRepository distributorTradeFailRepository;
    private final GiftCardBundleCommonService bundleCommonService;

    @PersistenceContext
    private EntityManager entityManager;

    public GiftCardBundleChargeCancelVo validateAndConvertRequestDto(GiftCardBundleChargeCancelRequestDto dto) {
        LocalDateTime now = DateTimeUtil.getCurrentDateTime();
        String trdDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String trdTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);
        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(dto.getReqDt()) && StringUtils.isNotEmpty(dto.getReqTm())) {
            mReqDtm = dto.getReqDt() + dto.getReqTm();
        }

        /* 원거래 조회 */
        GiftCardDistributorTrade gcTrd = distributorTradeRepository.findByDstbTrdNoAndTrdDt(dto.getOrgDstbTrdNo(), dto.getOrgTrdDt());
        if (gcTrd == null) {
            log.info("[GiftCardBundleChargeCancel][ERROR] Not exist original dstb trade | orgDstbTrdNo={} | orgTrdDt={}", dto.getOrgDstbTrdNo(), dto.getOrgTrdDt());
            throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_NOT_FOUND);
        }

        /* 신용카드 및 내통장 결제만 취소 가능 */
        if ("MPS".equals(gcTrd.getChrgMeanCd())) {
            log.info("[GiftCardBundleChargeCancel][ERROR] Cancel only CA or EZ");
            throw new RequestValidationException(ErrorCode.DISTRIBUTOR_BALANCE_DO_NOT_CANCEL_MPS_MONEY);
        }

        /* 유통 거래 번호가 취소내역인 경우 */
        if ("Y".equals(gcTrd.getCnclYn())) {
            log.info("[GiftCardBundleChargeCancel][ERROR] This is cancel trade | dstbTrdNo={} | trdDt={}", dto.getOrgDstbTrdNo(), dto.getOrgTrdDt());
            throw new RequestValidationException(ErrorCode.NOT_VALID_TRD_DIV_CD);
        }

        /* 취소 금액 확인 */
        if (gcTrd.getTrgAmt() != Long.parseLong(dto.getTrdAmt())) {
            log.info("[GiftCardBundleChargeCancel][ERROR] Amount is not matched | req={} | db={}", dto.getTrdAmt(), gcTrd.getTrgAmt());
            throw new RequestValidationException(ErrorCode.REQ_AMT_NOT_MATCHED);
        }

        /* 이미 취소 내역이 있는 경우 */
        GiftCardDistributorTrade orgGcTrd = distributorTradeRepository.findByOrgDstbTrdNoAndOrgTrdDt(dto.getOrgDstbTrdNo(), dto.getOrgTrdDt());
        if (orgGcTrd != null) {
            log.info("[GiftCardBundleChargeCancel][ERROR] Already cancelled | orgDstbTrdNo={} | orgTrdDt={}", dto.getOrgDstbTrdNo(), dto.getOrgTrdDt());
            throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_CANCELED);
        }

        String dstbTrdNo = bundleCommonService.generateDistributorTradeNo();
        log.info("[GiftCardBundleChargeCancel][STEP] Generate distributor trade number | dstbTrdNo: {}", dstbTrdNo);

        return GiftCardBundleChargeCancelVo.builder()
                .gcDstbNo(dto.getGcDstbNo())
                .dstbTrdNo(dstbTrdNo)
                .trdDt(trdDt)
                .trdTm(trdTm)
                .mReqDtm(mReqDtm)
                .mId(dto.getUseMid())
                .mTrdNo(dto.getMTrdNo())
                .trdAmt(dto.getTrdAmt())
                .orgDstbTrdNo(dto.getOrgDstbTrdNo())
                .orgTrdDt(dto.getOrgTrdDt())
                .createDate(now)
                .trdSumry(dto.getTrdSumry())
                .mResrvField1(dto.getMResrvField1())
                .mResrvField2(dto.getMResrvField2())
                .mResrvField3(dto.getMResrvField3())
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertDstbTradeFailWithCancel(GiftCardBundleChargeCancelRequestDto dto, ErrorCode errorCode) {
        String dstbTrdNo = bundleCommonService.generateDistributorTradeNo();

        /* 서버 시간 */
        LocalDateTime now = DateTimeUtil.getCurrentDateTime();
        String trdDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String trdTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);
        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(dto.getReqDt()) && StringUtils.isNotEmpty(dto.getReqTm())) {
            mReqDtm = dto.getReqDt() + dto.getReqTm();
        }

        GiftCardDistributorTradeFail tradeFail = GiftCardDistributorTradeFail.builder()
                .dstbTrdNo(dstbTrdNo)
                .failDt(trdDt)
                .failTm(trdTm)
                .mId(dto.getUseMid())
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.CHARGE.getPrdtCd())
                .cnclYn("Y")
                .mTrdNo(dto.getMTrdNo())
                .mReqDtm(mReqDtm)
                .trdDivCd(GcDstbTrdDivCd.CANCEL_DISTRIBUTOR.getCode())
                .amtSign(-1)
                .trgAmt(Long.parseLong(dto.getTrdAmt()))
                .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                .gcDstbNo(dto.getGcDstbNo())
                .errCd(errorCode.getErrorCode())
                .errMsg(errorCode.getErrorMessage())
                .trdSumry(dto.getTrdSumry())
                .mResrvField1(dto.getMResrvField1())
                .mResrvField2(dto.getMResrvField2())
                .mResrvField3(dto.getMResrvField3())
                .createdDate(now)
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();
        distributorTradeFailRepository.save(tradeFail);

        entityManager.flush();
    }

    @Transactional
    public long cancelDstbBalance(GiftCardBundleChargeCancelVo chargeCancelVo, GiftCardBundleDistributor distributor) {
        long dstbBlc = distributor.getDstbBlc() - Long.parseLong(chargeCancelVo.getTrdAmt());
        distributor.setDstbBlc(dstbBlc);
        distributor.setModifiedDate(chargeCancelVo.getCreateDate());
        distributor.setModifiedId(ServerInfoConfig.HOST_NAME);
        distributor.setModifiedIp(ServerInfoConfig.HOST_IP);

        // for error logging
        entityManager.flush();

        return dstbBlc;
    }

    @Transactional
    public void insertDstbTrade(GiftCardBundleChargeCancelVo chargeCancelVo) {
        long trdAmt = Long.parseLong(chargeCancelVo.getTrdAmt());

        /* 유통 거래 처리 */
        GiftCardDistributorTrade trade = GiftCardDistributorTrade.builder()
                .dstbTrdNo(chargeCancelVo.getDstbTrdNo())
                .trdDt(chargeCancelVo.getTrdDt())
                .trdTm(chargeCancelVo.getTrdTm())
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.CHARGE.getPrdtCd())
                .mId(chargeCancelVo.getMId())
                .mTrdNo(chargeCancelVo.getMTrdNo())
                .cnclYn("Y")
                .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                .trdDivCd(GcDstbTrdDivCd.CANCEL_DISTRIBUTOR.getCode())
                .gcDstbNo(chargeCancelVo.getGcDstbNo())
                .amtSign(-1)
                .trgAmt(trdAmt)
                .orgDstbTrdNo(chargeCancelVo.getOrgDstbTrdNo())
                .orgTrdDt(chargeCancelVo.getOrgTrdDt())
                .mReqDtm(chargeCancelVo.getMReqDtm())
                .trdSumry(chargeCancelVo.getTrdSumry())
                .mResrvField1(chargeCancelVo.getMResrvField1())
                .mResrvField2(chargeCancelVo.getMResrvField2())
                .mResrvField3(chargeCancelVo.getMResrvField3())
                .createdDate(chargeCancelVo.getCreateDate())
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();
        distributorTradeRepository.save(trade);

        // for error logging
        entityManager.flush();
    }
}
