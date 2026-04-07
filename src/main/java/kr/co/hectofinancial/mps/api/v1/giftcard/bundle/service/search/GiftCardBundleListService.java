package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundlePin;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleInfo;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleListRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleListResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.GiftCardBundleCommonService;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.api.v1.market.domain.MarketAddInfo;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleListService {

    private final GiftCardBundleCommonService commonService;
    private final JsonUtil jsonUtil;

    public GiftCardBundleListResponseDto getBundleList(GiftCardBundleListRequestDto dto) {
        log.info("[GiftCardBundleList][START] Get giftcard bundle list");

        try {
            /* 페이징 정보 셋팅 */
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

            /* 묶음 상품권 조회 */
            Pageable pageable = PageRequest.of(page, size);
            Page<GiftCardBundlePin> pageList = commonService.getGiftCardBundlePinByGcDstbNo(dto.getGcDstbNo(), pageable);

            long totCnt = pageList.getTotalElements();
            int totPage = pageList.getTotalPages();
            List<GiftCardBundlePin> gcBndlInfoList = pageList.toList();
            log.info("[GiftCardBundleList][STEP] Search list | totCnt: {} | totPage: {} | curPage: {}", totCnt, totPage, page);

            /* 상점 정보 조회 */
            Optional<MarketAddInfo> optionalMarketAddInfo = commonService.getMarketAddInfoByMId(dto.getUseMid());
            if (!optionalMarketAddInfo.isPresent()) {
                throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
            }

            MarketAddInfoDto marketAddInfo = MarketAddInfoDto.of(optionalMarketAddInfo.get());
            List<GiftCardBundleInfo> gcBndlList = new ArrayList<>();
            for (GiftCardBundlePin gcBndlInfo : gcBndlInfoList) {
                List<GiftCardIssueInfo> issList;
                try {
                    issList = jsonUtil.fromJsonArray(gcBndlInfo.getBndlReqInfo(), new TypeReference<List<GiftCardIssueInfo>>() {});
                } catch (JsonProcessingException e) {
                    log.error("Json parsing error: {}", e.getMessage(), e);
                    throw new RequestValidationException(ErrorCode.JSON_FORMAT_ERROR);
                }

                try {
                    GiftCardBundleInfo gcBndl = GiftCardBundleInfo.builder()
                            .gcBndlNo(CipherUtil.encrypt(commonService.decrypt(gcBndlInfo.getBndlPinNoEnc()), marketAddInfo.getEncKey()))
                            .gcBndlStatCd(gcBndlInfo.getPinStatCd())
                            .gcBndlAmt(String.valueOf(gcBndlInfo.getBndlAmt()))
                            .issDt(gcBndlInfo.getIssDt())
                            .issList(issList)
                            .build();

                    gcBndlList.add(gcBndl);
                } catch (Exception ex) {
                    log.info("[GiftCardBundleList][WARN] Fail to encrypt | gcBndlNo: {}", gcBndlInfo.getBndlPinNoEnc());
                }
            }

            log.info("[GiftCardBundleList][END] Get giftcard bundle list");

            return GiftCardBundleListResponseDto.builder()
                    .totCnt(String.valueOf(totCnt))
                    .totPage(String.valueOf(totPage))
                    .curPage(String.valueOf(page))
                    .useMid(dto.getUseMid())
                    .gcDstbNo(dto.getGcDstbNo())
                    .gcBndlList(gcBndlList)
                    .build();
        } catch (RequestValidationException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("묶음상품권 목록 조회 실패 | msg={}", e.getMessage(), e);
            alarmMtms(
                    dto.getGcDstbNo(),
                    dto.getUseMid(),
                    e.getMessage()
            );
            throw new RequestValidationException(ErrorCode.GIFT_CARD_BUNDLE_SEARCH_IS_ERROR);
        }
    }

    private static void alarmMtms(
            String gcDstbNo,
            String useMid,
            String errMsg
    ) {
        String message = String.format("**ERROR 발생** 묶음상품권 목록 조회 / " +
                "[" +
                "M_ID: " + useMid +
                ", 유통관리번호: " + gcDstbNo +
                ", 오류내용: " + errMsg +
                "][" + MDC.get("jsessionId") + "]"
        );
        MonitAgent.sendMonitAgent(ErrorCode.GIFT_CARD_BUNDLE_LIST_SEARCH_IS_ERROR.getErrorCode(), message);
    }
}
