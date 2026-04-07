package kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto;

import kr.co.hectofinancial.mps.api.v1.trade.domain.TrdFail;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TrdFailRepository;
import kr.co.hectofinancial.mps.global.constant.AutoChargeMethodType;
import kr.co.hectofinancial.mps.global.constant.AutoChargeType;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustAutoChargeMethod;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.PgPayRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.PgPayResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.NotiAutoChargeRequsetDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.service.AutoChargeNotiService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.error.exception.WhiteLabelException;
import kr.co.hectofinancial.mps.global.extern.whitelabel.constant.WhiteLabelErrorCode;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class PgPayService {

    private final AutoChargeNotiService autoChargeNotiService;
    private final PgPayServiceSupport pgPayServiceSupport;
    private final TrdFailRepository trdFailRepository;

    /**
     * 고객 충전 수단 갯수만큼 [머니충전 -> PG 결제] 시도
     * -> 최종 성공시 chrgTrdNo 거래테이블에 업데이트, 성공 노티
     * -> 최종 실패시 메인 충전수단 (=주계좌) 실패사유값을 거래실패 테이블에 저장, 실패 노티
     */

    public PgPayResponseDto payAndChargeMoney(CustomerDto customerDto, PgPayRequestDto pgPayRequestDto, List<CustAutoChargeMethod> chargeMethods, Supplier<Map<String, Long>> retryHandler) {

        //로깅용
        String mpsCustNo = pgPayRequestDto.getMpsCustNo();
        String mTrdNo = pgPayRequestDto.getMTrdNo();
        String mid = customerDto.getMid();

        //최종결과
        boolean isSuccess = false;
        Exception firstException = null;
        boolean isMnyBlcChanged = false;
        PgPayResponseDto pgPayResponseDto = null;
        log.info("[###][payAndChargeMoney][START] custNo={} mTrdNo={} ", mpsCustNo, mTrdNo);

        for (int i = 0; i < chargeMethods.size(); i++) {
            int tryCnt = (i + 1);
            try {
                CustAutoChargeMethod custAutoChargeMethod = chargeMethods.get(i);
                String code = custAutoChargeMethod.getPmtCode();
                String pmtKey = custAutoChargeMethod.getPmtKey();
                String pmtKeySuffix = custAutoChargeMethod.getPmtNoSuffix();
                String pmtName = custAutoChargeMethod.getPmtName();
                AutoChargeMethodType type = custAutoChargeMethod.getTypeAsEnum();//계좌, 카드 구분
                if (type == AutoChargeMethodType.ACCOUNT) {
                    if (CommonUtil.isBankMaintenanceHour()) {//은행점검시간
                        continue;
                    }
                    pgPayRequestDto.setBankAccountInfo(code, pmtKey, pmtKeySuffix);
                    if ("rrpayreg".equals(mid) || "paystbd2".equals(mid) || "M2471645".equals(mid)) {
                        pgPayRequestDto.setmResrvField1((pmtName + "(" + pmtKeySuffix + ")")); //은행명(계좌번호 뒷 3자리)
                        pgPayRequestDto.setTrdDivDtlCd("mAutoAccount"); //rrpay 요청사항 (trdDivCd)
                    }
                } else if (type == AutoChargeMethodType.CARD) {
                    pgPayRequestDto.setCardInfo(code, pmtKey, pmtKeySuffix);
                }
                pgPayRequestDto.setTryCnt(tryCnt);

                //PG연동 결제 및 머니충전
                pgPayResponseDto = pgPayServiceSupport.payWithWhiteLabelAndChargeMoney(customerDto, pgPayRequestDto);
                log.info("[###][payAndChargeMoney][SUCCESS] custNo={} mTrdNo={} tryCnt={}", mpsCustNo, mTrdNo, tryCnt);

                isSuccess = true;
                break;

            } catch (WhiteLabelException e) {
                log.error("[###][payAndChargeMoney][FAIL: {}] custNo={} mTrdNo={} tryCnt={} pgPagRequestDto={}", ErrorCode.WHITELABEL_API_FAIL.getErrorMessage(), mpsCustNo, mTrdNo, tryCnt, pgPayRequestDto, e);
                if (i == 0) firstException = e;
            } catch (RequestValidationException e) {
                log.error("[###][payAndChargeMoney][FAIL: {}] custNo={} mTrdNo={} tryCnt={} pgPagRequestDto={}", ErrorCode.CHARGE_MONEY_FAIL.getErrorMessage(), mpsCustNo, mTrdNo, tryCnt, pgPayRequestDto, e);

                if (i == 0) {
                    firstException = e;
                    //재시도 정책 (handler 로 넘어온)

                    if (AutoChargeType.THRESHOLD == pgPayRequestDto.getAutoChargeType()
                            && !isMnyBlcChanged
                            && e.getErrorCode() == ErrorCode.REQ_AMT_NOT_MATCHED
                            && retryHandler != null) {

                        isMnyBlcChanged = true;

                        Map<String, Long> mnyMap = retryHandler.get();
                        Long mnyBlc = mnyMap.get("mnyBlc");
                        Long reqMnyAmt = mnyMap.get("reqMnyAmt");

                        pgPayRequestDto.setBlcAmt(mnyBlc);
                        pgPayRequestDto.setReqAmt(reqMnyAmt);
                        i--;
                    }
                }
            } catch (Exception e) {
                log.error("[###][payAndChargeMoney][FAIL: {}] custNo={} mTrdNo={} tryCnt={} pgPagRequestDto={}", ErrorCode.AUTO_CHARGE_SYSTEM_FAIL.getErrorMessage(), mpsCustNo, mTrdNo, tryCnt, pgPayRequestDto, e);
                if (i == 0) firstException = e;
            }
        }
        if (isSuccess) {
            //MPS.PM_MPS_TRD Update
            pgPayServiceSupport.updateChrgTrdNo(pgPayResponseDto);

            //성공 노티
            sendSuccessNoti(pgPayResponseDto);
            return pgPayResponseDto;
        }
        if (firstException != null) {
            //실패 with Exception
            return handleChargeMoneyException(pgPayRequestDto, firstException);
        }
        //기본실패응답
        return PgPayResponseDto.failResponse(pgPayRequestDto, ErrorCode.AUTO_CHARGE_SYSTEM_ERROR);
    }

    /**
     * chargeMoney 최종 실패시 거래실패 insert, MTMS, 노티
     */
    private PgPayResponseDto handleChargeMoneyException(PgPayRequestDto pgPayRequestDto, Exception firstException) {
        ErrorCode basicError = ErrorCode.AUTO_CHARGE_SYSTEM_ERROR; //api 응답, 노티, 실패테이블, mtms 용

        String detailErrCd = "";
        String detailErrMsg = "";

        boolean saveFailTrd = true; //거래실패내역 쌓을지 구분값

        //로깅용
        String mpsCustNo = pgPayRequestDto.getMpsCustNo();
        String mTrdNo = pgPayRequestDto.getMTrdNo();
        if (firstException instanceof WhiteLabelException) {
            WhiteLabelException e = (WhiteLabelException) firstException;

            boolean mtmsFlag = false;

            if (!e.isCausedByValidation()) {
                Optional<ErrorCode> whiteLabelErrorCode = WhiteLabelErrorCode.resolveWhiteLabelErrorCode(e.getErrorCd());
                //정의안된 화이트라벨오류 혹은 계좌상태 오류일때만 괄호()안에 화이트라벨 진짜 에러메세지 넣어줌
                if (whiteLabelErrorCode.isPresent()) {
                    basicError = whiteLabelErrorCode.get();
                    if (basicError == ErrorCode.ACCOUNT_ERROR) {
                        detailErrCd = e.getErrorCd();//화라 에러코드
                        detailErrMsg = e.getErrorMsg();//화라 에러메세지
                    }
                    if (basicError != ErrorCode.BALANCE_IS_NOT_ENOUGH) {
                        mtmsFlag = true;
                    }
                } else {
                    mtmsFlag = true;

                    detailErrCd = e.getErrorCd();//화라 에러코드
                    detailErrMsg = e.getErrorMsg();//화라 에러메세지
                }
            }
            if (mtmsFlag) {
                //화라 연동 결과가 계좌 잔액부족 아닌경우에만 MTMS 울리기
                sendMtms(mpsCustNo, mTrdNo, ErrorCode.WHITELABEL_API_FAIL, e.getErrorCd(), e.getErrorMsg());
            }
        } else if (firstException instanceof RequestValidationException) {
            RequestValidationException e = (RequestValidationException) firstException;
            saveFailTrd = false; //얜 이미 충전프로시저실패하고 실패테이블에 쌓았음
            sendMtms(mpsCustNo, mTrdNo, ErrorCode.CHARGE_MONEY_FAIL, e.getErrorCode().getErrorCode(), e.getErrorCode().getErrorMessage());
        } else {

            sendMtms(mpsCustNo, mTrdNo, ErrorCode.AUTO_CHARGE_SYSTEM_FAIL, "", firstException.getMessage());
        }

        String notiMsg = basicError.getErrorMessage();
        if (StringUtils.isNotBlank(detailErrCd) && StringUtils.isNotBlank(detailErrMsg)) {
            notiMsg += MessageFormatter
                    .arrayFormat("({})", new Object[]{detailErrMsg})
                    .getMessage();
        }
        if (saveFailTrd) {
            /* Insert into MPS.PM_MPS_TRD_FAIL */
            pgPayServiceSupport.insertTradeFail(pgPayRequestDto, basicError.getErrorCode(), notiMsg);
        }

        /* sendFailNoti */
        sendFailNoti(pgPayRequestDto, basicError.getErrorCode(), notiMsg);

        /* PgPayResponseDto */
        return PgPayResponseDto.failResponse(pgPayRequestDto, basicError.getErrorCode(), notiMsg);
    }

    /**
     * 거래실패에 따른 MTMS 전송
     */
    private void sendMtms(String custNo, String mTrdNo, ErrorCode errorCode, String errCd, String errMsg) {
        errCd = StringUtils.isBlank(errCd) ? "NULL" : errCd; //errorCd 없는경우
        errMsg = errMsg.length() > 100 ? (errMsg.substring(0, 100) + "...") : errMsg; //errorMsg 200자로

        String additionalMsg = MessageFormatter
                .arrayFormat("custNo={} mTrdNo={} detailMsg=[errCd:{} errMsg:{}]", new Object[]{custNo, mTrdNo, errCd, errMsg})
                .getMessage();

        MonitAgent.sendMonitAgent(errorCode.getErrorCode(), errorCode.getErrorMessage() + additionalMsg);

    }

    /**
     * 가맹점향 성공 노티
     */
    private void sendSuccessNoti(PgPayResponseDto pgPayResponseDto) {

        log.info("[sendSuccessNoti][START] custNo={} mTrdNo={} chrgMeanCd={} trdNo={} trdDt={}",
                pgPayResponseDto.getMpsCustNo(), pgPayResponseDto.getMTrdNo(), pgPayResponseDto.getChrgMeanCd(), pgPayResponseDto.getTrdNo(), pgPayResponseDto.getTrdDt());

        NotiAutoChargeRequsetDto notiAutoChargeDto = new NotiAutoChargeRequsetDto();

        notiAutoChargeDto.setRsltCd(pgPayResponseDto.getResultCode());
        notiAutoChargeDto.setRsltMsg(pgPayResponseDto.getResultMsg());
        notiAutoChargeDto.setMid(pgPayResponseDto.getMid());
        notiAutoChargeDto.setCustNo(pgPayResponseDto.getMpsCustNo());
        notiAutoChargeDto.setMCustId(pgPayResponseDto.getMCustId());
        notiAutoChargeDto.setTrdNo(pgPayResponseDto.getTrdNo());
        notiAutoChargeDto.setTrdDt(pgPayResponseDto.getTrdDt());
        notiAutoChargeDto.setChargeType(pgPayResponseDto.getAutoChargeType().getValue());
        notiAutoChargeDto.setMnyAmt(String.valueOf(pgPayResponseDto.getReqAmt()));
        notiAutoChargeDto.setMnyBlc(String.valueOf(pgPayResponseDto.getPostMnyBlc()));
        notiAutoChargeDto.setChrgTrdNo(pgPayResponseDto.getChrgTrdNo());
        notiAutoChargeDto.setEncKey(pgPayResponseDto.getEncKey());
        notiAutoChargeDto.setPktHash(pgPayResponseDto.getPktHashKey());
        notiAutoChargeDto.setChrgTradeDate(pgPayResponseDto.getChrgDtm());
        notiAutoChargeDto.setBankCd(pgPayResponseDto.getBankCd());
        notiAutoChargeDto.setBankInfo(pgPayResponseDto.getCustAcntSuffix());

        try {
            
            autoChargeNotiService.sendAutoChargeInfo(notiAutoChargeDto);
        } catch (Exception e) {
            //노티자체에서 MTMS 발송완료

            log.error("[sendSuccessNoti][FAIL] custNo={} mTrdNo={} chrgMeanCd={} trdNo={} trdDt={}",
                    pgPayResponseDto.getMpsCustNo(), pgPayResponseDto.getMTrdNo(), pgPayResponseDto.getChrgMeanCd(), pgPayResponseDto.getTrdNo(), pgPayResponseDto.getTrdDt(), e);
        }

    }

    /**
     * 가맹점향 실패 노티
     */
    private void sendFailNoti(PgPayRequestDto pgPayRequestDto, String errorCd, String errorMsg) {

        log.info("[sendFailNoti][START] custNo={} mTrdNo={} chrgMeanCd={} errorCd={} errorMsg={}",
                pgPayRequestDto.getMpsCustNo(), pgPayRequestDto.getMTrdNo(), pgPayRequestDto.getChrgMeanCd().getChrgMeanCd(), errorCd, errorMsg);

        //기본값 현재시각
        String failDt = new CustomDateTimeUtil().getDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate now = LocalDate.now();
        String today = now.format(formatter);

        //12시 넘어가서 날짜 바뀌었을까봐 between 사용하기 위해 day -1 해줌
        String yesterday = now.minusDays(1).format(formatter);

        //실패거래내역에서 mTrdNo로 조회해서 failDt, failTm 가져다 쓰기
        List<TrdFail> trdFails = trdFailRepository.findTrdFailsBymTrdNoAndFailDtBetweenAndMpsCustNoAndTrdDivCd(
                pgPayRequestDto.getMTrdNo(),
                yesterday, today,
                pgPayRequestDto.getMpsCustNo(),
                TrdDivCd.MONEY_PROVIDE.getTrdDivCd()); //머니충전

        if (!trdFails.isEmpty()) {
            failDt = trdFails.get(0).getFailDt() + trdFails.get(0).getFailTm();
        }

        NotiAutoChargeRequsetDto notiAutoChargeDto = new NotiAutoChargeRequsetDto();

        notiAutoChargeDto.setRsltCd(errorCd);
        notiAutoChargeDto.setRsltMsg(errorMsg);
        notiAutoChargeDto.setMid(pgPayRequestDto.getMid());
        notiAutoChargeDto.setCustNo(pgPayRequestDto.getMpsCustNo());
        notiAutoChargeDto.setMCustId(pgPayRequestDto.getMCustId());
        notiAutoChargeDto.setTrdNo("");
        notiAutoChargeDto.setTrdDt("");
        notiAutoChargeDto.setChargeType(pgPayRequestDto.getAutoChargeType().getValue());
        notiAutoChargeDto.setMnyAmt(String.valueOf(pgPayRequestDto.getReqAmt())); //충전시도금액
        notiAutoChargeDto.setMnyBlc(String.valueOf(pgPayRequestDto.getBlcAmt()));//충전실패했으므로 충전시도 시점의 머니잔액
        notiAutoChargeDto.setChrgTrdNo("");
        notiAutoChargeDto.setEncKey(pgPayRequestDto.getEncKey());
        notiAutoChargeDto.setPktHash(pgPayRequestDto.getPktHashKey());
        notiAutoChargeDto.setTradeDate(failDt);
        notiAutoChargeDto.setBankCd(pgPayRequestDto.getBankCd());
        notiAutoChargeDto.setBankInfo(pgPayRequestDto.getCustAcntSuffix());

        try {

            autoChargeNotiService.sendAutoChargeInfo(notiAutoChargeDto);
        } catch (Exception e) {
            //노티자체에서 MTMS 발송완료

            log.error("[sendFailNoti][FAIL] custNo={} mTrdNo={} chrgMeanCd={} errorCd={} errorMsg={}",
                    pgPayRequestDto.getMpsCustNo(), pgPayRequestDto.getMTrdNo(), pgPayRequestDto.getChrgMeanCd().getChrgMeanCd(), errorCd, errorMsg, e);
        }

    }

}
