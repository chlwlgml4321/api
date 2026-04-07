package kr.co.hectofinancial.mps.api.v1.giftcard.single.service;

import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.constants.GcStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueVo;
import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarket;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Use.UseOut;
import kr.co.hectofinancial.mps.global.constant.PinVerifyResult;
import kr.co.hectofinancial.mps.global.constant.ProcResCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardIssueService {

    private final GiftCardIssueServiceSupport serviceSupport;

    @Transactional
    public GiftCardIssueResponseDto issue(GiftCardIssueRequestDto dto) {
        // 입력값 검증
        GiftCardIssueVo issueVo = serviceSupport.validateAndConvertRequestDto(dto);
        log.info("Validate and convert issueVo is success");

        try {
            // 핀 검증
            verifyPin(issueVo, dto);

            // 잔액 사용
            UseOut useOut = useBalance(issueVo);

            // 선불 거래내역 저장
            insertTradeSuccess(issueVo, useOut);

            // 상품권 발행
            List<GiftCardIssueInfo> reqIssInfoList = issueGiftCard(issueVo);
            log.info("gift card issue trdNo={}", issueVo.getTrdNo());

            return GiftCardIssueResponseDto.builder()
                    .custNo(issueVo.getMpsCustNo()) // 선불 고객 번호
                    .useMid(issueVo.getUseMid()) // 사용처 상점 아이디
                    .trdNo(issueVo.getTrdNo()) // 상품권 발행거래번호
                    .mTrdNo(issueVo.getMTrdNo()) // 상점 거래 번호
                    .vldDt(issueVo.getVldDt()) // 유효기간
                    .issDt(issueVo.getTrdDt()) // 발행일자
                    .issTm(issueVo.getTrdTm()) // 발행일시
                    .trdAmt(String.valueOf(issueVo.getTrdAmt())) // 총 발행금액
                    .mnyAmt(String.valueOf(useOut.getOutMnyAmt())) // 사용 머니
                    .pntAmt(String.valueOf(useOut.getOutPntAmt())) // 사용 포인트
                    .mnyBlc(String.valueOf(useOut.getOutMnyBlc())) // 사용 후 머니잔액
                    .pntBlc(String.valueOf(useOut.getOutPntBlc())) // 사용 후 포인트잔액
                    .gcStatCd(GcStatCd.ISSUE.getCode()) // 상품권 상태
                    .gcList(reqIssInfoList) // 상품권 발행 정보 (금액, 수량 및 상품권 번호)
                    .trdSumry(issueVo.getTrdSumry()) // 거래적요
                    .mResrvField1(issueVo.getMResrvField1()) // 예비필드1
                    .mResrvField2(issueVo.getMResrvField2()) // 예비필드2
                    .mResrvField3(issueVo.getMResrvField3()) // 예비필드3
                    .build();
        } catch (RequestValidationException ex) {
            log.error("Fail to issue gift card. msg={}", ex.getMessage(), ex);

            // 선불 거래실패내역 저장
            ErrorCode errorCode = ex.getErrorCode();
            serviceSupport.insertTradeFail(issueVo, errorCode.getErrorCode(), errorCode.getErrorMessage());

            throw ex;
        } catch (Exception ex) {
            log.error("Fail to issue gift card. msg={}", ex.getMessage(), ex);

            // 선불 거래실패내역 저장
            ErrorCode errorCode = ErrorCode.UNDEFINED_SERVER_ERROR_CODE;
            serviceSupport.insertTradeFail(issueVo, errorCode.getErrorCode(), errorCode.getErrorMessage());

            String message = String.format("**ERROR 발생** 선불상품권 발행 / " +
                    "[선불회원번호: " + issueVo.getMpsCustNo() +
                    ",회원 상점 아이디: " + issueVo.getMId() +
                    ",사용처 상점 아이디: " + issueVo.getUseMid() +
                    ",오류내용: " + ex.getMessage() +
                    "]");
            throw new RequestValidationException(ErrorCode.UNDEFINED_SERVER_ERROR_CODE, message);
        }
    }

    private void verifyPin(GiftCardIssueVo issueVo, GiftCardIssueRequestDto dto) {
        // 선불 회원 상점 아이디
        String mId = issueVo.getMId();

        // 핀 검증
        MpsMarket mpsMarket = serviceSupport.getMpsMarket(mId);
        if ("Y".equalsIgnoreCase(mpsMarket.getPinVrifyYn())) { // PIN 검증을 하는 경우
            ChkPinErrorCountResponseDto chkPinErrorCountResponseDto = serviceSupport.checkPinNumber(
                    issueVo.getTrdNo(),
                    dto.getPinNo(),
                    mpsMarket,
                    dto.getCustomerDto()
            );

            if (!PinVerifyResult.SUCCESS.equals(chkPinErrorCountResponseDto.getPinVerifyResult())) { // PIN 검증에 실패하면
                // 거래내역 실패 저장
                String errCd = chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorCode();
                String errMsg = chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorMessage() + chkPinErrorCountResponseDto.getPinVerifyResultMsgForTrdFail();

                // 선불 거래실패내역 저장
                serviceSupport.insertTradeFail(issueVo, errCd, errMsg);
                throw new RequestValidationException(ErrorCode.PIN_NOT_MATCHED, chkPinErrorCountResponseDto.getPinVerifyResultMsg());
            }
        } else { // PIN 검증을 하지 않는 경우
            log.info("###### CheckPin [End] => mId:[{}] pinVrifyYn is N", mId);
        }
    }

    private UseOut useBalance(GiftCardIssueVo issueVo) {
        try {
            UseOut useOut = serviceSupport.useBalance(issueVo); // 여기서 에러 발생 시 롤백 대상이 없기 때문에 종료

            // 잔액 사용 결과
            long resCode = useOut.getOutResCd();
            String resMsg = useOut.getOutResMsg();
            log.info("선불금 사용 EXEC => 회원번호:[{}] 결과코드:[{}] 결과메세지:[{}]", issueVo.getMpsCustNo(), resCode, resMsg);

            // 잔액 사용이 실패한 경우
            if (resCode != ProcResCd.SUCCESS.getResCd()) {
                // 선행처리로 인한 처리 거절. 선불 거래실패내역에 저장하지 않는다.
                if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
                    throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
                }

                ErrorCode errorCode = ErrorCode.SERVER_ERROR_CODE;
                if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
                    errorCode = ErrorCode.BALANCE_NOT_MATCHED;
                } else if (resCode == ProcResCd.REQ_AMT_NOT_MATCHED.getResCd()) {
                    errorCode = ErrorCode.REQ_AMT_NOT_MATCHED;
                } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd()) {
                    errorCode = ErrorCode.TRADE_AMT_ERROR;
                }

                throw new RequestValidationException(errorCode);
            }

            return useOut;
        } catch (DataAccessException | PersistenceException ex) {
            String message = String.format("**ERROR 발생** 선불상품권 발행 / " +
                    "[선불회원번호: " + issueVo.getMpsCustNo() +
                    ",회원 상점 아이디: " + issueVo.getMId() +
                    ",사용처 상점 아이디: " + issueVo.getUseMid() +
                    ",오류내용: 안심선불 사용 실패" +
                    "]");
            MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
            throw new RequestValidationException(ErrorCode.ERROR_GIFT_CARD_ISSUE);
        }
    }

    private void insertTradeSuccess(GiftCardIssueVo issueVo, UseOut useOut) {
        try {
            serviceSupport.insertTradeSuccess(issueVo, useOut);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GiftCardIssue][STEP] insertTradeSuccess is failure | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);

            String message = String.format("**ERROR 발생** 선불상품권 발행 / " +
                    "[선불회원번호: " + issueVo.getMpsCustNo() +
                    ",회원 상점 아이디: " + issueVo.getMId() +
                    ",사용처 상점 아이디: " + issueVo.getUseMid() +
                    ",오류내용: 안심선불 거래내역 저장 실패" +
                    "]");
            MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
            throw new RequestValidationException(ErrorCode.ERROR_GIFT_CARD_ISSUE);
        }
    }

    private List<GiftCardIssueInfo> issueGiftCard(GiftCardIssueVo issueVo) {
        try {
            return serviceSupport.issueGiftCard(issueVo);
        } catch (DataAccessException | PersistenceException ex) { /* 에러 발생 시 */
            log.error("[GiftCardIssue][STEP] issueGiftCard is failure | " +
                            "Exception: {} | Cause: {} | Message: {}",
                    ex.getClass(), ex.getCause(), ex.getMessage(), ex);

            String message = String.format("**ERROR 발생** 선불상품권 발행 / " +
                    "[선불회원번호: " + issueVo.getMpsCustNo() +
                    ",회원 상점 아이디: " + issueVo.getMId() +
                    ",사용처 상점 아이디: " + issueVo.getUseMid() +
                    ",오류내용: 선불상품권 생성 실패" +
                    "]");
            MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
            throw new RequestValidationException(ErrorCode.ERROR_GIFT_CARD_ISSUE);
        }
    }
}
