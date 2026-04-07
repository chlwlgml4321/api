package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcBndlStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundlePin;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleHistoryInfo;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleInfoRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleInfoResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.GiftCardBundleCommonService;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleInfoService {

    private final GiftCardBundleCommonService commonService;
    private final JsonUtil jsonUtil;

    @Transactional(readOnly = true)
    public GiftCardBundleInfoResponseDto getInfo(GiftCardBundleInfoRequestDto dto) {
        log.info("[GiftCardBundleInfo][START] Get giftcard bundle info");

        try {
            /* 묶음 상품권 조회 */
            String bndlPinNoEnc = commonService.encrypt(dto.getGcBndlNo());
            GiftCardBundlePin bundlePin;
            if (StringUtils.isBlank(dto.getIssDt())) {
                bundlePin = commonService.getGiftCardBundlePin(bndlPinNoEnc);
            } else {
                bundlePin = commonService.getGiftCardBundlePin(bndlPinNoEnc, dto.getIssDt());
            }
            log.info("[GiftCardBundleInfo][STEP] Giftcard bundle | info: {}", bndlPinNoEnc);
            if (bundlePin == null) {
                throw new RequestValidationException(ErrorCode.BUNDLE_GIFT_CARD_IS_NOT_EXIST);
            }

            /* 발행 이력 셋팅 */
            List<GiftCardBundleHistoryInfo> histList = new ArrayList<>();
            LocalDateTime createDate = bundlePin.getCreatedDate();
            String procDt = createDate.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String procTm = createDate.toLocalTime().format(DateTimeFormatter.ofPattern("HHmmss"));

            GiftCardBundleHistoryInfo info = GiftCardBundleHistoryInfo.builder()
                    .type(GcBndlStatCd.ISSUE.getCode())
                    .procDt(procDt)
                    .procTm(procTm)
                    .build();
            histList.add(info);

            /* 발행정보 */
            String issDt = procDt;
            String issTm = procTm;

            /* 최종상태가 사용, 양도 또는 만료인 경우 */
            if (GcBndlStatCd.USED.getCode().equals(bundlePin.getPinStatCd()) ||
                GcBndlStatCd.TRANSFER.getCode().equals(bundlePin.getPinStatCd()) ||
                GcBndlStatCd.EXPIRE.getCode().equals(bundlePin.getPinStatCd())) {
                LocalDateTime modifiedDate = bundlePin.getModifiedDate();
                procDt = modifiedDate.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                procTm = modifiedDate.toLocalTime().format(DateTimeFormatter.ofPattern("HHmmss"));

                info = GiftCardBundleHistoryInfo.builder()
                        .type(bundlePin.getPinStatCd())
                        .procDt(procDt)
                        .procTm(procTm)
                        .build();
                histList.add(info);
            }

            List<GiftCardIssueInfo> issList;
            try {
                issList = jsonUtil.fromJsonArray(bundlePin.getBndlReqInfo(), new TypeReference<List<GiftCardIssueInfo>>() {});
            } catch (JsonProcessingException e) {
                log.error("Json parsing error: {}", e.getMessage(), e);
                throw new RequestValidationException(ErrorCode.JSON_FORMAT_ERROR);
            }

            log.info("[GiftCardBundleInfo][END] Get giftcard bundle info");

            return GiftCardBundleInfoResponseDto.builder()
                    .gcDstbNo(bundlePin.getGcDstbNo())
                    .useMid(dto.getUseMid())
                    .gcBndlNo(dto.getGcBndlNo())
                    .gcBndlStatCd(bundlePin.getPinStatCd())
                    .issDt(issDt)
                    .issTm(issTm)
                    .vldDt(bundlePin.getVldPd())
                    .issList(issList)
                    .histList(histList)
                    .build();
        } catch (RequestValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("묶음상품권 조회 실패 | msg={}", e.getMessage(), e);
            alarmMtms(
                    commonService.encrypt(dto.getGcBndlNo()),
                    dto.getUseMid(),
                    e.getMessage()
            );
            throw new RequestValidationException(ErrorCode.GIFT_CARD_BUNDLE_SEARCH_IS_ERROR);
        }
    }

    private static void alarmMtms(
            String gcBndlNo,
            String useMid,
            String errMsg
    ) {
        String message = String.format("**ERROR 발생** 묶음상품권 조회 / " +
                "[" +
                "M_ID: " + useMid +
                ", 묵음상품권 번호: " + gcBndlNo +
                ", 오류내용: " + errMsg +
                "][" + MDC.get("jsessionId") + "]"
        );
        MonitAgent.sendMonitAgent(ErrorCode.GIFT_CARD_BUNDLE_SEARCH_IS_ERROR.getErrorCode(), message);
    }
}
