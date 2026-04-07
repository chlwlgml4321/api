package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service;

import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundlePin;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardBundleDistributorRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardBundlePinRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.service.GiftCardCommonService;
import kr.co.hectofinancial.mps.api.v1.market.domain.MarketAddInfo;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleCommonService {

    private final GiftCardBundleDistributorRepository distributorRepository;
    private final GiftCardBundlePinRepository pinRepository;
    private final GiftCardCommonService giftCardCommonService;
    private final SequenceService sequenceService;

    /**
     * 유통사 조회
     * @param gcDstbNo
     * @return
     */
    @Transactional(readOnly = true)
    public GiftCardBundleDistributor getDistributorWithLock(String gcDstbNo) {
        return distributorRepository.findByGcDstbNo(gcDstbNo);
    }

    @Transactional(readOnly = true)
    public GiftCardBundleDistributor getDistributor(String gcDstbNo) {
        return distributorRepository.findByGcDstbNoForReadOnly(gcDstbNo);
    }

    /**
     * 묶음 상품권 조회
     * @param bndlPinNoEnc
     * @return
     */
    @Transactional(readOnly = true)
    public GiftCardBundlePin getGiftCardBundlePinWithLock(String bndlPinNoEnc) {
        return pinRepository.findByBndlPinNoEnc(bndlPinNoEnc);
    }

    @Transactional(readOnly = true)
    public GiftCardBundlePin getGiftCardBundlePinWithLock(String bndlPinNoEnc, String issDt) {
        return pinRepository.findByBndlPinNoEncAndIssDt(bndlPinNoEnc, issDt);
    }

    @Transactional(readOnly = true)
    public GiftCardBundlePin getGiftCardBundlePin(String bndlPinNoEnc) {
        return pinRepository.findByBndlPinNoEncForReadOnly(bndlPinNoEnc);
    }

    @Transactional(readOnly = true)
    public GiftCardBundlePin getGiftCardBundlePin(String bndlPinNoEnc, String issDt) {
        return pinRepository.findByBndlPinNoEncAndIssDtForReadOnly(bndlPinNoEnc, issDt);
    }

    /**
     * 묶음 상품권 조회
     * @param gcDstbNo
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public Page<GiftCardBundlePin> getGiftCardBundlePinByGcDstbNo(String gcDstbNo, Pageable pageable) {
        return pinRepository.findAllByGcDstbNo(gcDstbNo, pageable);
    }

    @Transactional(readOnly = true)
    public String generateBndlPinNo(String mId) {
        return giftCardCommonService.generateGiftCardNo(mId);
    }

    public Optional<MarketAddInfo> getMarketAddInfoByMId(String mid) {
        return giftCardCommonService.getMarketAddInfoByMId(mid);
    }

    public String generateDistributorTradeNo() {
        return sequenceService.generateDistributorTradeSeq01();
    }

    public String maskingBndlPinNo(String gcNo) {
        // 앞 2자리, 뒤 4자리 마스킹
        return CommonUtil.maskingString(gcNo, 2, 4);
    }

    public String encrypt(String src) {
        return giftCardCommonService.encrypt(src);
    }

    public String decrypt(String enc) {
        return giftCardCommonService.decrypt(enc);
    }
}
