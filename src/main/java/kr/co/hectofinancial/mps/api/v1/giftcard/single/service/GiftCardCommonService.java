package kr.co.hectofinancial.mps.api.v1.giftcard.single.service;

import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.market.domain.MarketAddInfo;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GiftCardCommonService {

    private final CommonService commonService;
    private final SequenceService sequenceService;
    private final DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();

    public Optional<MarketAddInfo> getMarketAddInfoByMId(String mid) {
        return commonService.getOnlyMarketAddInfoByMId(mid);
    }

    public String generateGiftCardNo(String mId) {
        return sequenceService.generateGiftCardNo(mId);
    }

    public List generateGiftCardNoList(String mId, int size) {
        return sequenceService.generateGiftCardNoList(mId, size);
    }

    public String generateTradeNo() {
        return sequenceService.generateTradeSeq01();
    }

    public String maskingGiftCardNo(String gcNo) {
        // 앞 2자리, 뒤 4자리 마스킹
        return CommonUtil.maskingString(gcNo, 2, 4);
    }

    public String encrypt(String src) {
        return databaseAESCryptoUtil.convertToDatabaseColumn(src);
    }

    public String decrypt(String enc) {
        return databaseAESCryptoUtil.convertToEntityAttribute(enc);
    }
}
