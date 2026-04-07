package kr.co.hectofinancial.mps.api.v1.giftcard.single.service;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.constants.GcStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardUseResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardUseVo;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardUseService {

    private final GiftCardUseServiceSupport serviceSupport;

    @Transactional
    public GiftCardUseResponseDto useGiftCard(GiftCardUseRequestDto dto) {
        // 상품권 사용 입력값 확인
        GiftCardUseVo useVo = serviceSupport.validateAndConvertRequestDto(dto);

        try {
            List<String> gcNoEncList = useVo.getGcNoEncList();
            List<GiftCardIssue> gcInfoList = serviceSupport.getGiftCardList(gcNoEncList);
            log.info("Gift card list={}", gcInfoList);

            // 상품권 정보 셋팅. 미리하는 이유는 상품권 거래실패 테이블 내역에 저장해야하기 때문
            long trdAmt = 0;
            for (GiftCardIssue gcInfo : gcInfoList) {
                trdAmt += gcInfo.getIssAmt();
            }

            useVo.setGcIssueList(gcInfoList);
            useVo.setTrdAmt(trdAmt);

            // 등록되지 않은 상품권 확인
            checkToExistGiftCard(gcInfoList, gcNoEncList);

            // 상품권이 모두 사용가능한 상태인지 확인
            checkAllUsable(gcInfoList);

            // 상품권 사용 성공
            insertGiftCardTrade(useVo);

            // 상품권 사용 이력 저장
            insertGiftCardUseHistory(useVo);

            // 사용 완료 처리
            for (GiftCardIssue gcInfo : gcInfoList) {
                gcInfo.setGcStatCd(GcStatCd.USED.getCode());
                gcInfo.setBlc(0); // 잔액 모두 사용
                gcInfo.setModifiedDate(useVo.getCreateDate());
                gcInfo.setModifiedIp(ServerInfoConfig.HOST_IP);
                gcInfo.setModifiedId(ServerInfoConfig.HOST_NAME);
            }

            return GiftCardUseResponseDto.builder()
                    .mTrdNo(useVo.getMTrdNo()) // 상점 거래 번호
                    .gcTrdNo(useVo.getTrdNo()) // 사용 거래 번호
                    .useMid(useVo.getUseMid()) // 사용처 상점 아이디
                    .useGcList(useVo.getEncUseGcList()) // 사용 상품권 목록 (API 암호화)
                    .gcStatCd(GcStatCd.USED.getCode()) // 상품권 상태 (사용완료)
                    .trdAmt(String.valueOf(useVo.getTrdAmt())) // 총 사용 금액
                    .trdDt(useVo.getTrdDt()) // 사용 일자
                    .trdTm(useVo.getTrdTm()) // 사용 시간
                    .trdSumry(useVo.getTrdSumry()) // 거래 적요
                    .mResrvField1(useVo.getMResrvField1()) // 예비필드1
                    .mResrvField2(useVo.getMResrvField2()) // 예비필드2
                    .mResrvField3(useVo.getMResrvField3()) // 예비필드3
                    .build();
        } catch (RequestValidationException ex) {
            log.error("Fail to gift card use. msg={}", ex.getMessage(), ex);

            // 거래실패 저장
            serviceSupport.insertGiftCardTradeFail(useVo, ex.getErrorCode());

            throw ex;
        } catch (Exception ex) {
            log.error("Fail to gift card use. msg={}", ex.getMessage(), ex);

            // 거래실패 저장
            serviceSupport.insertGiftCardTradeFail(useVo, ErrorCode.UNDEFINED_SERVER_ERROR_CODE);

            String message = String.format("**ERROR 발생** 선불상품권 사용 / " +
                    "[상품권번호목록: " + useVo.getGcNoEncList() +
                    ",사용처 상점 아이디: " + useVo.getUseMid() +
                    ",오류내용: " + ex.getMessage() +
                    "]");
            throw new RequestValidationException(ErrorCode.UNDEFINED_SERVER_ERROR_CODE, message);
        }
    }

    private void checkToExistGiftCard(List<GiftCardIssue> gcInfoList, List<String> gcNoEncList) {
        if (gcNoEncList.size() != gcInfoList.size()) {
            log.info("Request gift card size={}", gcNoEncList.size());
            log.info("From db gift card size={}", gcInfoList.size());

            List<String> gcNoList = new ArrayList<>();
            for (GiftCardIssue gcInfo : gcInfoList) {
                gcNoList.add(gcInfo.getGcNoEnc());
            }

            Set<String> reqGcNoList = new HashSet<>(gcNoEncList);
            Set<String> dbGcNoList = new HashSet<>(gcNoList);

            reqGcNoList.removeAll(dbGcNoList);

            List<String> onlyReqGcNoList = new ArrayList<>(reqGcNoList);
            log.info("One or more giftcards are not registered. result={}", onlyReqGcNoList);

            throw new RequestValidationException(ErrorCode.GIFT_CARD_NOT_REGISTERED);
        }
    }

    private void checkAllUsable(List<GiftCardIssue> gcInfoList) {
        if (!gcInfoList.stream().allMatch(v -> v.getGcStatCd().equals(GcStatCd.ISSUE.getCode()))) {
            List<GiftCardIssue> invalidGcNoList = gcInfoList.stream()
                    .filter(v -> !GcStatCd.ISSUE.getCode().equals(v.getGcStatCd()))
                    .collect(Collectors.toList());

            List<String> gcNoList = new ArrayList<>();
            for (GiftCardIssue gcInfo : invalidGcNoList) {
                gcNoList.add(gcInfo.getGcNoEnc());
            }

            log.info("One or more gift cards are not available for use. result={}", gcNoList);

            throw new RequestValidationException(ErrorCode.GIFT_CARD_NOT_USABLE);
        }
    }

    private void insertGiftCardTrade(GiftCardUseVo useVo) {
        try {
            serviceSupport.insertGiftCardTradeSuccess(useVo);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GiftCardUse][STEP] insertGiftCardTrade is failure | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);

            String message = String.format("**ERROR 발생** 선불상품권 사용 / " +
                    "[상품권번호목록: " + useVo.getGcNoEncList() +
                    ",사용처 상점 아이디: " + useVo.getUseMid() +
                    ",오류내용: 상품권 거래내역 저장 실패" +
                    "]");
            MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
            throw new RequestValidationException(ErrorCode.ERROR_GIFT_CARD_USE);
        }
    }

    private void insertGiftCardUseHistory(GiftCardUseVo useVo) {
        try {
            serviceSupport.insertGiftCardUseHistory(useVo);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GiftCardUse][STEP] insertGiftCardUseHistory is failure | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);

            String message = String.format("**ERROR 발생** 선불상품권 사용 / " +
                    "[상품권번호목록: " + useVo.getGcNoEncList() +
                    ",사용처 상점 아이디: " + useVo.getUseMid() +
                    ",오류내용: 상품권 사용 이력 저장 실패" +
                    "]");
            MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
            throw new RequestValidationException(ErrorCode.ERROR_GIFT_CARD_USE);
        }
    }
}
