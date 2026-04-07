package kr.co.hectofinancial.mps.api.v1.giftcard.single.service;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.constants.GcStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardReissueRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardReissueResponseDto;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardReIssueService {

    private final GiftCardReIssueServiceSupport serviceSupport;
    private final GiftCardCommonService giftCardCommonService;

    @Transactional
    public GiftCardReissueResponseDto reIssue(GiftCardReissueRequestDto dto) {
        LocalDateTime now = DateTimeUtil.getCurrentDateTime();
        String procDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String procTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);

        // 상품권 요청 정보 확인
        String bfGcNoEnc = giftCardCommonService.encrypt(dto.getGcNo());

        // 상품권 상태 확인 (사용가능만 변경 가능)
        log.info("Request gcNoEnc is {}", bfGcNoEnc);
        GiftCardIssue gcIssue = serviceSupport.getGiftCardIssue(bfGcNoEnc);
        log.info("Gift card info is {}", gcIssue);

        if (gcIssue == null) { // 상품권번호가 잘못된 경우
            log.info("This gift card number is not registered. gcNoEnc is {}", bfGcNoEnc);
            throw new RequestValidationException(ErrorCode.GIFT_CARD_NOT_FOUND);
        }

        if (!GcStatCd.ISSUE.getCode().equals(gcIssue.getGcStatCd())) {
            log.info("This gift card is not a re issuable state. gcStatCd is {}", gcIssue.getGcStatCd());
            throw new RequestValidationException(ErrorCode.GIFT_CARD_NOT_REISSUABLE_STATE);
        }

        // 금액 확인
        if (gcIssue.getIssAmt() != Integer.parseInt(dto.getGcAmt())) {
            log.info("The requested gift card amount is invalid. reqAmt={}, dbAmt={}", dto.getGcAmt(), gcIssue.getIssAmt());
            throw new RequestValidationException(ErrorCode.GIFT_CARD_REISSUE_INVALID_AMOUNT);
        }

        try {
            // 기존 상품권 상태 변경 (교체)
            String bfGcStatCd = GcStatCd.REISSUE.getCode();
            gcIssue.setGcStatCd(bfGcStatCd);
            gcIssue.setModifiedDate(now);
            gcIssue.setModifiedId(ServerInfoConfig.HOST_NAME);
            gcIssue.setModifiedIp(ServerInfoConfig.HOST_IP);

            // 신규 상품권 발행
            String gcNo = generateGiftCard(gcIssue, now);
            log.info("bfGcNoEnc is {}", bfGcNoEnc);
            log.info("gcNoEnc is {}", giftCardCommonService.encrypt(gcNo));

            return GiftCardReissueResponseDto.builder()
                    .gcNo(gcNo)
                    .gcAmt(String.valueOf(gcIssue.getIssAmt()))
                    .gcStatCd(GcStatCd.ISSUE.getCode())
                    .bfGcNo(dto.getGcNo())
                    .bfGcStatCd(bfGcStatCd)
                    .useMid(dto.getUseMid())
                    .vldDt(gcIssue.getVldPd())
                    .procDt(procDt)
                    .procTm(procTm)
                    .build();
        } catch (RequestValidationException ex) {
            log.error("Fail to re-issue gift card. msg={}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Fail to re-issue gift card. msg={}", ex.getMessage(), ex);

            String message = String.format("**ERROR 발생** 선불상품권 재발행 / " +
                    "[사용처 상점 아이디: " + gcIssue.getUseMid() +
                    ",이전 상품권 번호: " + gcIssue.getGcNoEnc() +
                    ",오류내용: " + ex.getMessage() +
                    "]");
            throw new RequestValidationException(ErrorCode.UNDEFINED_SERVER_ERROR_CODE, message);
        }
    }

    private String generateGiftCard(GiftCardIssue gcIssue, LocalDateTime updateDate) {
        try {
            return serviceSupport.generateGiftCard(gcIssue, updateDate);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GiftCardReIssue][STEP] generateGiftCard is failure | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);

            String message = String.format("**ERROR 발생** 선불상품권 재발행 / " +
                    "[사용처 상점 아이디: " + gcIssue.getUseMid() +
                    ",이전 상품권 번호: " + gcIssue.getGcNoEnc() +
                    ",오류내용: 상품권 생성 실패 " +
                    "]");
            MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
            throw new RequestValidationException(ErrorCode.ERROR_GIFT_CARD_REISSUE);
        }
    }
}
