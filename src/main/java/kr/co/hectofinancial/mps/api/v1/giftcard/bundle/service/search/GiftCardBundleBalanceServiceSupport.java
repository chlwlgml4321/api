package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.search;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.DstbPrdtCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcDstbTrdDivCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTrade;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTradeFail;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.GiftCardBundleBalanceUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.cancel.GiftCardBundleBalanceUseCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardBundleDistributorRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardDistributorTradeFailRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardDistributorTradeRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.MpsApiCd;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleBalanceServiceSupport {

    private final GiftCardBundleDistributorRepository distributorRepository;
    private final GiftCardDistributorTradeRepository distributorTradeRepository;
    private final GiftCardDistributorTradeFailRepository distributorTradeFailRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public long updateBalance(GiftCardBundleDistributor distributor, long dstbBlc) {
        /* 잔액 사용 */
        distributor.setDstbBlc(dstbBlc);
        distributor.setModifiedDate(LocalDateTime.now());
        distributor.setModifiedId(ServerInfoConfig.HOST_NAME);
        distributor.setModifiedIp(ServerInfoConfig.HOST_IP);
        distributorRepository.save(distributor);

        // for error logging
        entityManager.flush();

        return dstbBlc;
    }

    @Transactional
    public void insertDstbTrade(
            GiftCardBundleBalanceUseRequestDto dto,
            String dstbTrdNo,
            String trdDt,
            String trdTm,
            String mReqDtm
    ) {
        /* 유통 거래 이력 저장 */
        GiftCardDistributorTrade trade = GiftCardDistributorTrade.builder()
                .dstbTrdNo(dstbTrdNo)
                .trdDt(trdDt)
                .trdTm(trdTm)
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.USE.getPrdtCd())
                .cnclYn("N")
                .mId(dto.getUseMid())
                .mTrdNo(dto.getMTrdNo())
                .mReqDtm(mReqDtm)
                .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                .trdDivCd(GcDstbTrdDivCd.DISTRIBUTOR_DECREASE.getCode())
                .gcDstbNo(dto.getGcDstbNo())
                .amtSign(-1)
                .trgAmt(Long.parseLong(dto.getTrdAmt()))
                .trdSumry(dto.getTrdSumry())
                .mResrvField1(dto.getMResrvField1())
                .mResrvField2(dto.getMResrvField2())
                .mResrvField3(dto.getMResrvField3())
                .createdDate(LocalDateTime.now())
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();
        distributorTradeRepository.save(trade);

        // for error logging
        entityManager.flush();
    }

    @Transactional
    public void insertDstbTradeForCancel(GiftCardBundleBalanceUseCancelRequestDto dto,
                                         String dstbTrdNo,
                                         String trdDt,
                                         String trdTm,
                                         String mReqDtm) {
        /* 유통 거래 이력 저장 */
        GiftCardDistributorTrade trade = GiftCardDistributorTrade.builder()
                .dstbTrdNo(dstbTrdNo)
                .trdDt(trdDt)
                .trdTm(trdTm)
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.USE.getPrdtCd())
                .cnclYn("N")
                .mId(dto.getUseMid())
                .mTrdNo(dto.getMTrdNo())
                .mReqDtm(mReqDtm)
                .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                .trdDivCd(GcDstbTrdDivCd.WITHDRAW_DISTRIBUTOR.getCode())
                .gcDstbNo(dto.getGcDstbNo())
                .amtSign(1)
                .orgDstbTrdNo(dto.getOrgDstbTrdNo())
                .orgTrdDt(dto.getOrgTrdDt())
                .trgAmt(Long.parseLong(dto.getTrdAmt()))
                .trdSumry(dto.getTrdSumry())
                .mResrvField1(dto.getMResrvField1())
                .mResrvField2(dto.getMResrvField2())
                .mResrvField3(dto.getMResrvField3())
                .createdDate(LocalDateTime.now())
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();
        distributorTradeRepository.save(trade);

        // for error logging
        entityManager.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertDstbTradeFail(
            GiftCardBundleBalanceUseRequestDto dto,
            String dstbTrdNo,
            String failDt,
            String failTm,
            String mReqDtm,
            ErrorCode errorCode) {
        GiftCardDistributorTradeFail tradeFail = GiftCardDistributorTradeFail.builder()
                .dstbTrdNo(dstbTrdNo)
                .failDt(failDt)
                .failTm(failTm)
                .mId(dto.getUseMid())
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.USE.getPrdtCd())
                .cnclYn("N")
                .mTrdNo(dto.getMTrdNo())
                .mReqDtm(mReqDtm)
                .trdDivCd(GcDstbTrdDivCd.DISTRIBUTOR_DECREASE.getCode())
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
                .createdDate(LocalDateTime.now())
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();

        distributorTradeFailRepository.save(tradeFail);
        entityManager.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertDstbTradeFailForCancel(
            GiftCardBundleBalanceUseCancelRequestDto dto,
            String dstbTrdNo,
            String failDt,
            String failTm,
            String mReqDtm,
            ErrorCode errorCode
    ) {
        GiftCardDistributorTradeFail tradeFail = GiftCardDistributorTradeFail.builder()
                .dstbTrdNo(dstbTrdNo)
                .failDt(failDt)
                .failTm(failTm)
                .mId(dto.getUseMid())
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.USE.getPrdtCd())
                .cnclYn("N")
                .mTrdNo(dto.getMTrdNo())
                .mReqDtm(mReqDtm)
                .trdDivCd(GcDstbTrdDivCd.WITHDRAW_DISTRIBUTOR.getCode())
                .amtSign(1)
                .trgAmt(Long.parseLong(dto.getTrdAmt()))
                .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                .gcDstbNo(dto.getGcDstbNo())
                .orgDstbTrdNo(dto.getOrgDstbTrdNo())
                .orgTrdDt(dto.getOrgTrdDt())
                .errCd(errorCode.getErrorCode())
                .errMsg(errorCode.getErrorMessage())
                .trdSumry(dto.getTrdSumry())
                .mResrvField1(dto.getMResrvField1())
                .mResrvField2(dto.getMResrvField2())
                .mResrvField3(dto.getMResrvField3())
                .createdDate(LocalDateTime.now())
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();

        distributorTradeFailRepository.save(tradeFail);
        entityManager.flush();
    }
}
