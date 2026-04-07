package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.charge;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.DstbPrdtCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcDstbTrdDivCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTrade;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTradeFail;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.GiftCardBundleChargeEtcRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.GiftCardBundleChargeVo;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardBundleDistributorRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardDistributorTradeFailRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardDistributorTradeRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.GiftCardBundleCommonService;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.MpsApiCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
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
public class GiftCardBundleChargeEtcServiceSupport {

    private final GiftCardBundleDistributorRepository distributorRepository;
    private final GiftCardDistributorTradeRepository distributorTradeRepository;
    private final GiftCardDistributorTradeFailRepository distributorTradeFailRepository;
    private final GiftCardBundleCommonService bundleCommonService;

    @PersistenceContext
    private EntityManager entityManager;

    public GiftCardBundleChargeVo validateAndConvertRequestDto(GiftCardBundleChargeEtcRequestDto dto) {
        long trdAmt = Long.parseLong(dto.getTrdAmt());

        /* 서버 시간 */
        LocalDateTime now = DateTimeUtil.getCurrentDateTime();
        String trdDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String trdTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);
        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(dto.getReqDt()) && StringUtils.isNotEmpty(dto.getReqTm())) {
            mReqDtm = dto.getReqDt() + dto.getReqTm();
        }

        /* 유통 거래 번호 생성 */
        String dstbTrdNo = bundleCommonService.generateDistributorTradeNo();
        log.info("[GcBndlChrgEtc][STEP] Generate distributor trade number | dstbTrdNo: {}", dstbTrdNo);

        return GiftCardBundleChargeVo.builder()
                .useMid(dto.getUseMid())
                .gcDstbNo(dto.getGcDstbNo()) // 유통 관리 번호
                .mTrdNo(dto.getMTrdNo()) // 상점 거래 번호
                .mReqDtm(mReqDtm)
                .trdDivCd(GcDstbTrdDivCd.DISTRIBUTOR_CHARGE.getCode())
                .chrgMeanCd(dto.getChrgMeanCd()) // 충전수단코드
                .trdNo(dto.getChrgTrdNo()) // 충전거래번호
                .trdDt(trdDt) // 거래 일자
                .trdTm(trdTm) // 거래 시간
                .trdAmt(trdAmt) // 총 발행 금액
                .stlMid(dto.getUseMid()) // 정산대상 상점아이디
                .mResrvField1(dto.getMResrvField1()) // 예비필드1
                .mResrvField2(dto.getMResrvField2()) // 예비필드2
                .mResrvField3(dto.getMResrvField3()) // 예비필드3
                .trdSumry(dto.getTrdSumry()) // 거래적요
                .createDate(now) // 서버 생성 시간
                .dstbTrdNo(dstbTrdNo)
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertDstbTradeFail(GiftCardBundleChargeVo chargeVo, ErrorCode errorCode) {
        GiftCardDistributorTradeFail tradeFail = GiftCardDistributorTradeFail.builder()
                .dstbTrdNo(chargeVo.getDstbTrdNo())
                .failDt(chargeVo.getTrdDt())
                .failTm(chargeVo.getTrdTm())
                .mId(chargeVo.getUseMid())
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.CHARGE.getPrdtCd())
                .cnclYn("N")
                .mTrdNo(chargeVo.getMTrdNo())
                .mReqDtm(chargeVo.getMReqDtm())
                .trdDivCd(chargeVo.getTrdDivCd())
                .amtSign(1)
                .trgAmt(chargeVo.getTrdAmt())
                .chrgMeanCd(chargeVo.getChrgMeanCd())
                .gcDstbNo(chargeVo.getGcDstbNo())
                .errCd(errorCode.getErrorCode())
                .errMsg(errorCode.getErrorMessage())
                .trdSumry(chargeVo.getTrdSumry())
                .mResrvField1(chargeVo.getMResrvField1())
                .mResrvField2(chargeVo.getMResrvField2())
                .mResrvField3(chargeVo.getMResrvField3())
                .createdDate(chargeVo.getCreateDate())
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();

        distributorTradeFailRepository.save(tradeFail);

        // for error logging
        entityManager.flush();
    }

    @Transactional
    public long chargeDstbBalance(GiftCardBundleChargeVo chargeVo, GiftCardBundleDistributor distributor) {
        long dstbBlc;
        /* 잔액 정보가 없으면 insert */
        if (distributor == null) {
            dstbBlc = chargeVo.getTrdAmt();

            GiftCardBundleDistributor newDistributor = GiftCardBundleDistributor.builder()
                    .gcDstbNo(chargeVo.getGcDstbNo())
                    .dstbBlc(dstbBlc)
                    .createdDate(chargeVo.getCreateDate())
                    .createdIp(ServerInfoConfig.HOST_IP)
                    .createdId(ServerInfoConfig.HOST_NAME)
                    .build();
            distributorRepository.save(newDistributor);
        } else {
            dstbBlc = distributor.getDstbBlc() + chargeVo.getTrdAmt();
            distributor.setDstbBlc(dstbBlc);
            distributor.setModifiedDate(chargeVo.getCreateDate());
            distributor.setModifiedId(ServerInfoConfig.HOST_NAME);
            distributor.setModifiedIp(ServerInfoConfig.HOST_IP);
        }

        // for error logging
        entityManager.flush();

        return dstbBlc;
    }

    @Transactional
    public void insertDstbTrade(GiftCardBundleChargeVo chargeVo) {
        /* 유통 거래 이력 */
        GiftCardDistributorTrade trade = GiftCardDistributorTrade.builder()
                .dstbTrdNo(chargeVo.getDstbTrdNo())
                .trdDt(chargeVo.getTrdDt())
                .trdTm(chargeVo.getTrdTm())
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.CHARGE.getPrdtCd())
                .cnclYn("N")
                .mId(chargeVo.getUseMid())
                .mTrdNo(chargeVo.getMTrdNo())
                .mReqDtm(chargeVo.getMReqDtm())
                .chrgMeanCd(chargeVo.getChrgMeanCd())
                .chrgTrdNo(chargeVo.getTrdNo())
                .trdDivCd(chargeVo.getTrdDivCd())
                .gcDstbNo(chargeVo.getGcDstbNo())
                .amtSign(1)
                .trgAmt(chargeVo.getTrdAmt())
                .trdSumry(chargeVo.getTrdSumry())
                .mResrvField1(chargeVo.getMResrvField1())
                .mResrvField2(chargeVo.getMResrvField2())
                .mResrvField3(chargeVo.getMResrvField3())
                .createdDate(chargeVo.getCreateDate())
                .createdIp(ServerInfoConfig.HOST_IP)
                .createdId(ServerInfoConfig.HOST_NAME)
                .build();

        /* 유통 거래 이력 저장 */
        distributorTradeRepository.save(trade);

        // for error logging
        entityManager.flush();
    }
}
