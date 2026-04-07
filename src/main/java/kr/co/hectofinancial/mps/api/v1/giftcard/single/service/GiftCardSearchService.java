package kr.co.hectofinancial.mps.api.v1.giftcard.single.service;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.constants.GcStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.*;
import kr.co.hectofinancial.mps.api.v1.market.domain.MarketAddInfo;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardSearchService {

    private final GiftCardSearchServiceSupport serviceSupport;
    private final GiftCardCommonService giftCardCommonService;

    public GiftCardSearchResponseDto getGiftCardList(GiftCardSearchRequestDto dto) {
        log.info("Request gcNoEnc or issTrdNo is {}", giftCardCommonService.encrypt(dto.getGcInfo()));
        log.info("Request issDt is {}", dto.getIssDt());

        // 상품권 정보 조회
        int page = 0;
        if (dto.getPage() != null) {
            page = dto.getPage() - 1;
            if (page < 0) {
                page = 0;
            }
        }

        int size = 10;
        if (dto.getSize() != null) {
            size = dto.getSize();
            if (size > 50) {
                size = 50;
            }
        }

        GiftCardSearchResult result = serviceSupport.getGiftCardInfo(dto.getGcInfo(), dto.getIssDt(), page, size);
        List<GiftCardIssue> gcIssueList = result.getGcIssList();
        log.info("Gift card info is {}", gcIssueList);

        Optional<MarketAddInfo> optionalMarketAddInfo = giftCardCommonService.getMarketAddInfoByMId(dto.getUseMid());
        if (!optionalMarketAddInfo.isPresent()) {
            throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
        }

        MarketAddInfoDto mIdInfo = MarketAddInfoDto.of(optionalMarketAddInfo.get());
        try {
            List<GiftCardSearch> gcInfoList = new ArrayList<>();
            for (GiftCardIssue gcIssue : gcIssueList) {
                // 기본 정보 셋팅
                GiftCardSearch gcInfo = GiftCardSearch.builder()
                        .gcNo(CipherUtil.encrypt(giftCardCommonService.decrypt(gcIssue.getGcNoEnc()), mIdInfo.getEncKey()))
                        .gcAmt(String.valueOf(gcIssue.getIssAmt()))
                        .vldDt(gcIssue.getVldPd())
                        .gcStatCd(gcIssue.getGcStatCd())
                        .build();

                // 이력 셋팅
                List<GiftCardHistoryInfo> histList = new ArrayList<>();

                // 최초 발행
                String createDate = DateTimeUtil.convertStringDateTime(gcIssue.getCreatedDate()); // 발행일시
                GiftCardHistoryInfo historyInfo = GiftCardHistoryInfo.builder()
                        .type(GcStatCd.ISSUE.getCode())
                        .date(createDate.substring(0, 8))
                        .time(createDate.substring(8, 14))
                        .trdNo(gcIssue.getIssTrdNo())
                        .build();
                histList.add(historyInfo);

                // 상태 수정 내역이 있는 경우
                if (gcIssue.getModifiedDate() != null) {
                    String updateDate = DateTimeUtil.convertStringDateTime(gcIssue.getModifiedDate()); // 수정일시

                    if (GcStatCd.EXPIRE.getCode().equals(gcIssue.getGcStatCd())) {
                        GiftCardHistoryInfo expireInfo = GiftCardHistoryInfo.builder()
                                .type(GcStatCd.EXPIRE.getCode())
                                .date(updateDate.substring(0, 8))
                                .time(updateDate.substring(8, 14))
                                .build();
                        histList.add(expireInfo);
                    } else { // 사용, 사용 취소, 재발행
                        // 사용 또는 취소인 경우
                        List<Object[]> useHistoryList = serviceSupport.getUseHistory(gcIssue.getGcNoEnc(), gcIssue.getIssDt());
                        for (Object[] useHistory : useHistoryList) {
                            GiftCardHistoryInfo useHistoryInfo = GiftCardHistoryInfo.builder()
                                    .trdNo(String.valueOf(useHistory[0]))
                                    .date(String.valueOf(useHistory[1]))
                                    .time(String.valueOf(useHistory[2]))
                                    .type(String.valueOf(useHistory[3]))
                                    .trdSumry(useHistory[4] != null ? String.valueOf(useHistory[4]) : null)
                                    .mResrvField1(useHistory[5] != null ? String.valueOf(useHistory[5]) : null)
                                    .mResrvField2(useHistory[6] != null ? String.valueOf(useHistory[6]) : null)
                                    .mResrvField3(useHistory[7] != null ? String.valueOf(useHistory[7]) : null)
                                    .build();
                            histList.add(useHistoryInfo);
                        }

                        if (GcStatCd.REISSUE.getCode().equals(gcIssue.getGcStatCd())) { // 마지막 상태가 교체인 경우
                            // 신규 상품권 번호 확인
                            GiftCardIssue newGcIssue = serviceSupport.getGiftCardIssueByBfGcNoEncAndBfIssDt(gcIssue.getGcNoEnc(), gcIssue.getIssDt());
                            String newGcNo = null;
                            if (newGcIssue != null) {
                                newGcNo = CipherUtil.encrypt(giftCardCommonService.decrypt(newGcIssue.getGcNoEnc()), mIdInfo.getEncKey());
                            }

                            GiftCardHistoryInfo reissueInfo = GiftCardHistoryInfo.builder()
                                    .type(GcStatCd.REISSUE.getCode())
                                    .date(updateDate.substring(0, 8))
                                    .time(updateDate.substring(8, 14))
                                    .newGcNo(newGcNo)
                                    .build();
                            histList.add(reissueInfo);
                        }
                    }
                }

                gcInfo.setHistList(histList);

                // add list
                gcInfoList.add(gcInfo);
            }

            return GiftCardSearchResponseDto.builder()
                    .totCnt(String.valueOf(result.getTotCnt()))
                    .totPage(String.valueOf(result.getTotPage()))
                    .curPage(String.valueOf(page + 1))
                    .useMid(dto.getUseMid())
                    .gcList(gcInfoList)
                    .build();
        } catch (Exception ex) {
            log.error("Fail to search gift card. msg={}", ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage()); // MTMS 알림
        }
    }
}
