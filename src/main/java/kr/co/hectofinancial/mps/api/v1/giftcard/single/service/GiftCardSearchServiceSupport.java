package kr.co.hectofinancial.mps.api.v1.giftcard.single.service;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardSearchResult;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.repository.GiftCardIssueRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.repository.GiftCardUseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardSearchServiceSupport {

    private final GiftCardIssueRepository issueRepository;
    private final GiftCardUseRepository useRepository;
    private final GiftCardCommonService giftCardCommonService;

    @Transactional(readOnly = true)
    public GiftCardIssue getGiftCardIssueByBfGcNoEncAndBfIssDt(String bfGcNoEnc, String bfIssDt) {
        return issueRepository.findByBfGcNoEncAndBfIssDt(bfGcNoEnc, bfIssDt);
    }

    public GiftCardSearchResult getGiftCardInfo(String gcInfo, String issDt, int page, int size) {
        String gcNoEnc = ""; // 상품권 번호
        String issTrdNo = ""; // 상품권 발행번호

        if (gcInfo.length() == 35) {
            gcNoEnc = giftCardCommonService.encrypt(gcInfo);
        } else {
            issTrdNo = gcInfo;
        }

        Page<GiftCardIssue> pageList;
        if (StringUtils.isEmpty(issDt)) {
            if (StringUtils.isEmpty(issTrdNo)) {
                pageList = issueRepository.findGiftCardIssueList(gcNoEnc, PageRequest.of(page, size));
            } else {
                pageList = issueRepository.findByGcIssTrdNo(issTrdNo, PageRequest.of(page, size));
            }
        } else {
            if (StringUtils.isEmpty(issTrdNo)) {
                pageList = issueRepository.findByGcNoEncAndIssDt(gcNoEnc, issDt, PageRequest.of(page, size));
            } else {
                pageList = issueRepository.findByGcIssTrdNoAndIssDt(issTrdNo, issDt, PageRequest.of(page, size));
            }
        }

        long totCnt = pageList.getTotalElements();
        int totPage = pageList.getTotalPages();
        List<GiftCardIssue> gcIssList = pageList.toList();

        return GiftCardSearchResult.builder()
                .totCnt(totCnt)
                .totPage(totPage)
                .curPage(page)
                .gcIssList(gcIssList)
                .build();
    }

    public List<Object[]> getUseHistory(String gcNoEnc, String issDt) {
        return useRepository.findGcUseHistory(gcNoEnc, issDt);
    }
}
