package kr.co.hectofinancial.mps.api.v1.trade.service.charge;

import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.notification.dto.PyNtcSendInsertRequestDto;
import kr.co.hectofinancial.mps.api.v1.notification.service.NotiService;
import kr.co.hectofinancial.mps.api.v1.trade.domain.AdminTrdReq;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.dto.*;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminChargeApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalCancelResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Pay.PayIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Pay.PayOut;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.PayCancel.PayCancelIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.PayCancel.PayCancelOut;
import kr.co.hectofinancial.mps.api.v1.trade.repository.AdminTrdReqRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.api.v1.trade.service.TradeFailService;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.*;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

    private final SequenceService sequenceService;
    private final TradeRepository tradeRepository;
    private final TradeFailService tradeFailService;
    private final CommonService commonService;
    private final NotiService notiService;
    private final AdminTrdReqRepository adminTrdReqRepository;

    /* 머니, 포인트 충전 */
    public ChargeApprovalResponseDto chargeApproval(ChargeApprovalRequestDto chargeApprovalRequestDto) {

        log.info("회원번호: [{}], 상점거래번호: [{}], 요청금액: [{}]", chargeApprovalRequestDto.getCustomerDto().getMpsCustNo(), chargeApprovalRequestDto.getMTrdNo(), chargeApprovalRequestDto.getTrdAmt());
        String reqDtm = "";
        String trdNo = "";
        String vldPd = "";
        String pntId = "";
        Long trdAmt = 0L;
        Long mnyAmt = 0L;
        Long pntAmt = 0L;
        Long waitAmt = 0L;
        Long mnyBlc = 0L;
        Long pntBlc = 0L;
        Long waitMnyBlc;
        String blsUseOrd = "";
        long chrgAmtReq;
        String chrgTrdNo = null;
        String trdSumry = "";

        /* 일자 시각 SET */
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();
        if (CommonUtil.nullTrim(chargeApprovalRequestDto.getReqDt()).equals("")) {
            reqDtm = curDt + curTm;
        } else {
            String reqTm = (chargeApprovalRequestDto.getReqTm() == null ? curTm : chargeApprovalRequestDto.getReqTm());
            reqDtm = chargeApprovalRequestDto.getReqDt() + reqTm;
        }

        if (!CommonUtil.nullTrim(chargeApprovalRequestDto.getTrdSumry()).equals("")) {
            trdSumry = chargeApprovalRequestDto.getTrdSumry();
        }

        String custNo = chargeApprovalRequestDto.getCustNo();
        String mid = chargeApprovalRequestDto.getCustomerDto().getMid();
        String trdDivCd = chargeApprovalRequestDto.getDivCd();
        String mCustId = chargeApprovalRequestDto.getCustomerDto().getMCustId();

        /* 거래번호 */
        trdNo = sequenceService.generateTradeSeq01();

        /* 회원조회 */
        CustomerDto customer = chargeApprovalRequestDto.getCustomerDto();

        if (CommonUtil.nullTrim(chargeApprovalRequestDto.getTrdAmt()).equals("0") || CommonUtil.nullTrim(chargeApprovalRequestDto.getTrdAmt()).equals("")) {
            throw new RequestValidationException(ErrorCode.TRADE_AMT_ERROR);
        }

        chrgAmtReq = Long.parseLong(CommonUtil.nullTrim(chargeApprovalRequestDto.getTrdAmt()));

        /* 외부충전거래번호 */
        if (!CommonUtil.nullTrim(chargeApprovalRequestDto.getChrgTrdNo()).equals("")) {
            chrgTrdNo = CommonUtil.nullTrim(chargeApprovalRequestDto.getChrgTrdNo());
        }

        //충전 최소금액&단위 검증
        commonService.checkMinChrgAmt(mid, chargeApprovalRequestDto.getChrgMeanCd(), chargeApprovalRequestDto.getDivCd(), chrgAmtReq);

        /* 거래상세구분코드 검증 */
        if (!CommonUtil.nullTrim(chargeApprovalRequestDto.getTrdDivDtlCd()).equals("")) {
            commonService.checkValidTrdDivDtlCd(mid, CommonUtil.nullTrim(trdDivCd), CommonUtil.nullTrim(chargeApprovalRequestDto.getTrdDivDtlCd()));
        } else {
            chargeApprovalRequestDto.setTrdDivDtlCd("");
        }

        /* 검증부 시작 */
        //거래구분코드 & 충전수단코드 검증
        if (CommonUtil.nullTrim(trdDivCd).equals(TrdDivCd.MONEY_PROVIDE.getTrdDivCd())) {

            //외부충전거래번호 검증
            if (!CommonUtil.nullTrim(chargeApprovalRequestDto.getChrgMeanCd()).equals(TrdChrgMeanCd.PIN.getChrgMeanCd())) {
                if (chrgTrdNo == null) {
                    throw new RequestValidationException(ErrorCode.CHRG_TRD_NO_ERROR);
                }
            }

            //무기명회원 충전수단 검증
            if (customer.getCustDivCd().equals(CustDivCd.ANONYMOUS.getCustDivCd())) {
                //2026-01-20 정책 변경 -> 무기명 회원 CA 도 허용
                //2026-02-04 기존 라운드(무기명회원)만 PIN 허용해주던 로직 제외 -> 충전수단에 등록하는것으로 관리하도록
                //2026-02-05 정책 변경 -> 무기명 회원 가상계좌 도 허용
                if (!chargeApprovalRequestDto.getChrgMeanCd().equals(TrdChrgMeanCd.ZOZ.getChrgMeanCd())
                        && !chargeApprovalRequestDto.getChrgMeanCd().equals(TrdChrgMeanCd.PIN.getChrgMeanCd())
                        && !chargeApprovalRequestDto.getChrgMeanCd().equals(TrdChrgMeanCd.CREDITCARD_APPROVAL.getChrgMeanCd())
                        && !chargeApprovalRequestDto.getChrgMeanCd().equals(TrdChrgMeanCd.VIRTUAL_ACCOUNT.getChrgMeanCd())) {
                    throw new RequestValidationException(ErrorCode.CUSTOMER_UNINSCRIBED);
                }
            }
            mnyBlc = Long.parseLong(chargeApprovalRequestDto.getBlcAmt());
        } else if (CommonUtil.nullTrim(trdDivCd).equals(TrdDivCd.POINT_PROVIDE.getTrdDivCd())) {
            if (!CommonUtil.nullTrim(chargeApprovalRequestDto.getChrgMeanCd()).equals(TrdChrgMeanCd.HECTO_POINT.getChrgMeanCd())
                    && !CommonUtil.nullTrim(chargeApprovalRequestDto.getChrgMeanCd()).equals(TrdChrgMeanCd.CPN_POINT.getChrgMeanCd())
                    && !CommonUtil.nullTrim(chargeApprovalRequestDto.getChrgMeanCd()).equals(TrdChrgMeanCd.POINT_MIGRATION.getChrgMeanCd())) {
                throw new RequestValidationException(ErrorCode.TRADE_CHRG_MEAN_CD_ERROR);
            }
            pntBlc = Long.parseLong(chargeApprovalRequestDto.getBlcAmt());
        } else {
            throw new RequestValidationException(ErrorCode.TRADE_DIV_CD_ERROR);
        }
        /* 검증부 종료 */

        /* 수수료 값 제외 충전 */

        Long custBdnFeeAmt;
        if (CommonUtil.nullTrim(chargeApprovalRequestDto.getCustBdnFeeAmt()).equals("")) {
            custBdnFeeAmt = 0L;
        } else if (Long.parseLong(CommonUtil.nullTrim(chargeApprovalRequestDto.getCustBdnFeeAmt())) < 0) {
            throw new RequestValidationException(ErrorCode.CUST_FEE_AMT_ERROR);
        } else {
            custBdnFeeAmt = Long.parseLong(CommonUtil.nullTrim(chargeApprovalRequestDto.getCustBdnFeeAmt()));
        }

        if (chargeApprovalRequestDto.getDivCd().equals(TrdDivCd.POINT_PROVIDE.getTrdDivCd())) {
            custBdnFeeAmt = 0L;
            pntAmt = chrgAmtReq;
            blsUseOrd = TrBlcUseOrd.POINT.getBlcUseOrd();
            trdAmt = pntAmt;
//            chargeApprovalRequestDto.setChrgMeanCd(TrdChrgMeanCd.CPN_POINT.getChrgMeanCd());//CP
            pntId = "CP000001"; //TODO 하드코딩 수정

            /* 포인트 유효기간 */
            if (CommonUtil.nullTrim(chargeApprovalRequestDto.getPntVldPd()).equals("")) {
                throw new RequestValidationException(ErrorCode.POINT_VALIDITY_NOT_VALID);
            } else {
                vldPd = chargeApprovalRequestDto.getPntVldPd();
                if (Long.parseLong(vldPd) < Long.parseLong(curDt)) {
                    throw new RequestValidationException(ErrorCode.POINT_VALIDITY_PERIOD_ERROR);
                }
                String maxVldPd = DateTimeUtil.addYear(customDateTimeUtil.getDate(), 10);
                if (Long.parseLong(vldPd) > Long.parseLong(maxVldPd)) {
                    throw new RequestValidationException(ErrorCode.POINT_VALIDITY_LIMIT_REACHED);
                }
            }
        } else if (chargeApprovalRequestDto.getDivCd().equals(TrdDivCd.MONEY_PROVIDE.getTrdDivCd())) {
            blsUseOrd = TrBlcUseOrd.MONEY.getBlcUseOrd();
            mnyAmt = chrgAmtReq - custBdnFeeAmt;
            trdAmt = mnyAmt;
        } else if (chargeApprovalRequestDto.getDivCd().equals(TrdDivCd.WAITMONEY_PROVIDE.getTrdDivCd())) {
            custBdnFeeAmt = 0L;
            waitAmt = chrgAmtReq;
            trdAmt = waitAmt;
            blsUseOrd = TrBlcUseOrd.MONEY.getBlcUseOrd();
        }

        log.info("충전금액(프로시저IN) : [{}]", trdAmt);

        /* 프로시저 호출 */
        PayIn payIn = PayIn.builder()
                .inMpsCustNo(custNo)
                .inChrgMeanCd(CommonUtil.nullTrim(chargeApprovalRequestDto.getChrgMeanCd()))
                .inTrdDivCd(trdDivCd)
                .inTrdDivDtlCd(CommonUtil.nullTrim(chargeApprovalRequestDto.getTrdDivDtlCd()))
                .inCustDivCd(customer.getCustDivCd())
                .inChrgLmtAmt(customer.getChrgLmtAmlt())
                .inMID(mid)
                .inPayTrdNo(trdNo)
                .inPayTrdDt(curDt)
                .inTrdAmt(trdAmt)
                .inBlc(Long.parseLong(chargeApprovalRequestDto.getBlcAmt()))
                .inVldPd(vldPd)
                .inPntId(pntId) //임시
                .inPayRsn(trdSumry)
                .inWorkerID(ServerInfoConfig.HOST_NAME)
                .inWorkerIP(ServerInfoConfig.HOST_IP)
                .build();
        PayOut payOut = tradeRepository.doPay(payIn);

        long resCode = (long) payOut.getOutResCd();
        String resMsg = payOut.getOutResMsg();
        log.info("응답코드: [{}] 응답메시지: [{}]", resCode, resMsg);

        if (resCode == ProcResCd.SUCCESS.getResCd()) {

            /* 고객 잔액 SET */
            mnyBlc = (Long) payOut.getOutMnyBlc();
            pntBlc = (Long) payOut.getOutPntBlc();
            waitMnyBlc = (Long) payOut.getOutWaitMnyBlc();
            log.info("머니: [{}] 포인트: [{}], 대기머니: [{}]", mnyBlc, pntBlc, waitMnyBlc);

            //정상거래 set
            tradeRepository.save(Trade.builder()
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdDivCd(trdDivCd)
                    .svcCd(MpsApiCd.SVC_CD)
                    .prdtCd(MpsPrdtCd.charge.getPrdtCd())
                    .mid(mid)
                    .amtSign(+1)
                    .trdAmt(Long.parseLong(chargeApprovalRequestDto.getTrdAmt()))
                    .mnyAmt(mnyAmt)
                    .pntAmt(pntAmt)
                    .waitMnyAmt(waitAmt)
                    .mnyBlc(mnyBlc)
                    .pntBlc(pntBlc)
                    .waitMnyBlc(waitMnyBlc)
                    .blcUseOrd(blsUseOrd)
                    .chrgMeanCd(chargeApprovalRequestDto.getChrgMeanCd())
                    .mReqDtm(reqDtm)
                    .mTrdNo(chargeApprovalRequestDto.getMTrdNo())
                    .mpsCustNo(custNo)
                    .mCustId(mCustId)
                    .custBdnFeeAmt(custBdnFeeAmt)
                    .trdSumry(trdSumry)
                    .createdIp(ServerInfoConfig.HOST_IP)
                    .createdId(ServerInfoConfig.HOST_NAME)
                    .chrgTrdNo(chrgTrdNo)
                    .storNm("N")
                    .storCd("N")
                    .stlMId(mid)
                    .trdDivDtlCd(chargeApprovalRequestDto.getTrdDivDtlCd())
                    .mResrvField1(chargeApprovalRequestDto.getMResrvField1())
                    .mResrvField2(chargeApprovalRequestDto.getMResrvField2())
                    .mResrvField3(chargeApprovalRequestDto.getMResrvField3())
                    .build()
            );

            /* 충전 성공 충전 알림 메일 전송 */
            if (trdDivCd.equals(TrdDivCd.MONEY_PROVIDE.getTrdDivCd())) {
                if (!CommonUtil.nullTrim(customer.getEmail()).equals("")) {
                    try {
                        String trdDtm;
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        trdDtm = now.format(formatter);

                        String formattedMoney = CommonUtil.formatMoney(trdAmt);

                        PyNtcSendInsertRequestDto pyNtcSendInsertRequestDto = PyNtcSendInsertRequestDto.builder()
                                .mpsCustNo(custNo)
                                .custNm(customer.getCustNm())
                                .email(customer.getEmail())
                                .trdNo(trdNo)
                                .amt(formattedMoney)
                                .chrgMeanCd(CommonUtil.nullTrim(chargeApprovalRequestDto.getChrgMeanCd()))
                                .trdDtm(trdDtm)
                                .mid(mid)
                                .msgTmplId("MPS_TRD_03")
                                .build();
                        notiService.savePyNtcSend(pyNtcSendInsertRequestDto);
                    } catch (Exception e) {
                        log.error("[충전 알림 메일 발송 실패] 회원번호: [{}], 거래번호: [{}]", custNo, trdNo);
                        String message = String.format("[선불회원번호: " + custNo + ", 거래번호: " + trdNo + "]");
                        MonitAgent.sendMonitAgent(ErrorCode.NOTI_SERVICE_FAIL.getErrorCode(), message);
                        e.printStackTrace();
                    }
                }
            }

            return ChargeApprovalResponseDto.builder()
                    .custNo(custNo)
                    .mTrdNo(chargeApprovalRequestDto.getMTrdNo())
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdAmt(String.valueOf(trdAmt))
                    .custBdnFeeAmt(String.valueOf(custBdnFeeAmt))
                    .mnyBlc(String.valueOf(mnyBlc)) //거래 후 잔액
                    .pntBlc(String.valueOf(pntBlc))
                    .chrgTrdNo(chrgTrdNo)
                    .trdDivDtlCd(chargeApprovalRequestDto.getTrdDivDtlCd())
                    .build();

        } else {

            if (!chargeApprovalRequestDto.getChrgMeanCd().equals(TrdChrgMeanCd.POINT_TO_MONEY.getChrgMeanCd())) { //2025-05-28 전환일때 실패테이블에 한줄만 쌓도록 수정 -> TF거래쪽에서 쌓기
                TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
                tradeFailInsertDto.setTrdNo(trdNo);
                tradeFailInsertDto.setCustNo(custNo);
                tradeFailInsertDto.setMCustId(mCustId);
                tradeFailInsertDto.setTrdDivCd(trdDivCd);
                tradeFailInsertDto.setMid(mid);
                tradeFailInsertDto.setFailDt(curDt);
                tradeFailInsertDto.setFailTm(curTm);
                tradeFailInsertDto.setMTrdNo(chargeApprovalRequestDto.getMTrdNo());
                tradeFailInsertDto.setAmtSign(+1);
                tradeFailInsertDto.setTrdAmt(trdAmt);
                tradeFailInsertDto.setMnyAmt(mnyAmt);
                tradeFailInsertDto.setPntAmt(pntAmt);
                tradeFailInsertDto.setWaitMnyAmt(waitAmt);
                tradeFailInsertDto.setMnyBlc(mnyBlc);
                tradeFailInsertDto.setPntBlc(pntBlc);
                tradeFailInsertDto.setWaitMnyBlc((Long) payOut.getOutWaitMnyBlc());
                tradeFailInsertDto.setCustBdnFeeAmt(custBdnFeeAmt);
                tradeFailInsertDto.setBlcUseOrd(blsUseOrd);
                tradeFailInsertDto.setChrgMeanCd(chargeApprovalRequestDto.getChrgMeanCd());
                tradeFailInsertDto.setReqDtm(reqDtm);
                tradeFailInsertDto.setTrdSumry(chargeApprovalRequestDto.getTrdSumry());
                tradeFailInsertDto.setErrCd(String.valueOf(resCode));
                tradeFailInsertDto.setErrMsg(resMsg);
                tradeFailInsertDto.setPrdtCd(MpsPrdtCd.charge.getPrdtCd());
                tradeFailInsertDto.setChrgTrdNo(chrgTrdNo);
                tradeFailInsertDto.setTrdDivDtlCd(chargeApprovalRequestDto.getTrdDivDtlCd());
                tradeFailInsertDto.setMResrvField1(chargeApprovalRequestDto.getMResrvField1());
                tradeFailInsertDto.setMResrvField2(chargeApprovalRequestDto.getMResrvField2());
                tradeFailInsertDto.setMResrvField3(chargeApprovalRequestDto.getMResrvField3());
                log.info("지급 거래실패 data : {} ", tradeFailInsertDto);
                tradeFailService.insertTradeFail(tradeFailInsertDto);
            }
        }

        if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);

        } else if (resCode == ProcResCd.LIMIT_REACHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.CHARGE_LIMIT_REACHED);

        } else if (resCode == ProcResCd.MONTHLY_LIMIT_REACHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.MONTHLY_LIMIT_REACHED);

        } else if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);

        } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd() || resCode == ProcResCd.ERROR.getResCd()) {
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE);
        } else {
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE, " [충전 오류]");
        }

    }

    /* 머니, 포인트 충전취소 */
    public ChargeApprovalCancelResponseDto chargeApprovalCancel(ChargeApprovalCancelRequestDto chargeApprovalCancelRequestDto) {

        log.info("회원번호: [{}], 상점거래번호: [{}]", chargeApprovalCancelRequestDto.getCustomerDto().getMpsCustNo(), chargeApprovalCancelRequestDto.getMTrdNo());

        String reqDtm = "";

        long cancelReqAmt = 0L;   // 요청 취소금액
        long cancelAmt = 0L;      // 실제 취소금액 (프로시저 전달)
        long cancelMnyAmt = 0L;   // 머니 취소금액
        long cancelPntAmt = 0L;   // 포인트 취소금액
        long mnyBlc = 0L;
        long pntBlc = 0L;
        long waitMnyBlc = 0L;
        String blcUseOrd = "";

        /* 일자 시각 SET */
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();

        if (CommonUtil.nullTrim(chargeApprovalCancelRequestDto.getReqDt()).equals("")) {
            reqDtm = curDt + curTm;

        } else {
            String reqTm = (chargeApprovalCancelRequestDto.getReqTm() == null ? curTm : chargeApprovalCancelRequestDto.getReqTm());
            reqDtm = chargeApprovalCancelRequestDto.getReqDt() + reqTm;
        }

        String trdNo = sequenceService.generateTradeSeq01();
        String mid = chargeApprovalCancelRequestDto.getCustomerDto().getMid();
        String custNo = chargeApprovalCancelRequestDto.getCustNo();
        String trdDivCd = chargeApprovalCancelRequestDto.getDivCd();
        String mCustId = chargeApprovalCancelRequestDto.getCustomerDto().getMCustId();

        /* 취소 요청금액 SET */
        if (!CommonUtil.nullTrim(chargeApprovalCancelRequestDto.getCnclAmt()).equals("")) {
            cancelReqAmt = Long.parseLong(chargeApprovalCancelRequestDto.getCnclAmt());
            log.info("취소 요청금액 : [{}]", cancelReqAmt);
        }

        /* 거래구분코드 검증 */
        if (!CommonUtil.nullTrim(trdDivCd).equals(TrdDivCd.MONEY_CANCEL.getTrdDivCd()) &&
                !CommonUtil.nullTrim(trdDivCd).equals(TrdDivCd.POINT_CANCEL.getTrdDivCd())) {
            throw new RequestValidationException(ErrorCode.TRADE_DIV_CD_ERROR);
        }

        /* 원거래 조회 */
        Trade orgTrade = tradeRepository.findTradeByMpsCustNoAndTrdNoAndTrdDt(custNo, chargeApprovalCancelRequestDto.getOrgTrdNo(), chargeApprovalCancelRequestDto.getOrgTrdDt())
                .orElseThrow(() -> new RequestValidationException(ErrorCode.TRADE_ORIGINAL_NOT_FOUND));


        /* 원거래건의 거래구분코드 검증 */
        if (!TrdDivCd.MONEY_PROVIDE.getTrdDivCd().equals(orgTrade.getTrdDivCd()) && !TrdDivCd.POINT_PROVIDE.getTrdDivCd().equals(orgTrade.getTrdDivCd())) {
            log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] TRD_DIV_CD:[{}] 원거래건의 거래구분 코드가 [충전]이 아님", chargeApprovalCancelRequestDto.getOrgTrdNo(), chargeApprovalCancelRequestDto.getOrgTrdDt(), orgTrade.getTrdDivCd());
            throw new RequestValidationException(ErrorCode.NOT_VALID_TRD_DIV_CD);

        }

        if (orgTrade.getCnclTrdAmt() == orgTrade.getTrdAmt()) {
            //원거래건의 취소거래금액==원거래건의 거래금액 이면 전체취소 건
            throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_CANCELED);
        }

        /* 충전취소 가능여부 조회 */
        String chrgCnclPlcCd = commonService.checkChrgCnclPsblYn(mid, orgTrade.getChrgMeanCd(), orgTrade.getTrdDt());

        if (chrgCnclPlcCd.equals(ChrgCnclPlcCd.FULL_ONLY.getChrgCnclPlcCd())) {

            if (cancelReqAmt > 0) {
                if (orgTrade.getTrdAmt() != cancelReqAmt) {
                    log.info("부분취소 불가능한 충전수단 => [{}]", orgTrade.getChrgMeanCd());
                    throw new RequestValidationException(ErrorCode.NOT_POSSIBLE_CHARGE_CANCEL, "(부분취소 불가능)");
                }
            }
        }

        /* 취소 금액 계산 */
        if (cancelReqAmt == 0) {

            log.info("======= (충전취소 - 전체취소 요청) 요청금액: [{}] =======", cancelReqAmt);
            cancelAmt = orgTrade.getTrdAmt(); //전체취소 요청

        } else {

            log.info("======= (충전취소 - 부분취소 인입됨) 요청금액: [{}] =======", cancelReqAmt);
            if (cancelReqAmt < 0) {
                throw new RequestValidationException(ErrorCode.TRADE_AMT_ERROR);
            }

            if (cancelReqAmt > orgTrade.getTrdAmt()) {
                log.info("부분취소 요청금액 [{}] 이 원거래금액 [{}] 보다 큼 ", cancelReqAmt, orgTrade.getTrdAmt());
                throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
            }

            if (orgTrade.getTrdAmt() - orgTrade.getCnclTrdAmt() < cancelReqAmt) {
                log.info("부분취소 요청금액 [{}] 이 취소가능 금액 [{}] 보다 큼 ", cancelReqAmt, orgTrade.getTrdAmt() - orgTrade.getCnclTrdAmt());
                throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
            }

            cancelAmt = cancelReqAmt;

        }

        log.info("최종 취소금액 : [{}]", cancelAmt);

        if (trdDivCd.equals(TrdDivCd.MONEY_CANCEL.getTrdDivCd())) {

            if (!orgTrade.getTrdDivCd().equals(TrdDivCd.MONEY_PROVIDE.getTrdDivCd())) {
                throw new RequestValidationException(ErrorCode.TRADE_DIV_CD_ERROR);
            }

            blcUseOrd = TrBlcUseOrd.MONEY.getBlcUseOrd();
            cancelMnyAmt = cancelAmt;
            cancelPntAmt = 0;

        } else {

            if (!orgTrade.getTrdDivCd().equals(TrdDivCd.POINT_PROVIDE.getTrdDivCd())) {
                throw new RequestValidationException(ErrorCode.TRADE_DIV_CD_ERROR);
            }

            blcUseOrd = TrBlcUseOrd.POINT.getBlcUseOrd();
            cancelPntAmt = cancelAmt;
            cancelMnyAmt = 0;

        }

        /* 프로시저 호출 */
        PayCancelIn doPayCancelIn = PayCancelIn.builder()
                .inMpsCustNo(custNo)
                .inTrdDivCd(trdDivCd)
                .inPayTrdNo(orgTrade.getTrdNo())
                .inPayTrdDt(orgTrade.getTrdDt())
                .inUseTrDt(curDt)
                .inUseTrNo(trdNo)
                .inTrdAmt(cancelAmt) //취소요청금액
                .inBlc(Long.parseLong(chargeApprovalCancelRequestDto.getBlcAmt()))
                .inWorkerID(ServerInfoConfig.HOST_NAME)
                .inWorkerIP(ServerInfoConfig.HOST_IP)
                .build();

        PayCancelOut payCancelOut = tradeRepository.doPayCancel(doPayCancelIn);

        long resCode = (long) payCancelOut.getOutResCd();
        String resMsg = payCancelOut.getOutResMsg();

        log.info("응답코드 :{} 응답메시지: {}", resCode, resMsg);
        if (resCode == ProcResCd.SUCCESS.getResCd()) {

            /* 고객 잔액 */
            mnyBlc = (Long) payCancelOut.getOutMnyBlc();
            pntBlc = (Long) payCancelOut.getOutPntBlc();
            waitMnyBlc = (Long) payCancelOut.getOutWaitMnyBlc();

            log.info("머니: [{}] 포인트: [{}], 대기머니: [{}]", mnyBlc, pntBlc, waitMnyBlc);

            /* 정상거래 저장 */
            tradeRepository.save(Trade.builder()
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdDivCd(trdDivCd)
                    .svcCd(MpsApiCd.SVC_CD)
                    .prdtCd(MpsPrdtCd.charge.getPrdtCd())
                    .mid(mid)
                    .amtSign(-1)
                    .trdAmt(cancelAmt)
                    .mnyAmt(cancelMnyAmt)
                    .pntAmt(cancelPntAmt)
                    .waitMnyAmt(0)
                    .mnyBlc(mnyBlc)
                    .pntBlc(pntBlc)
                    .waitMnyBlc(waitMnyBlc)
                    .blcUseOrd(blcUseOrd)
                    .chrgMeanCd(orgTrade.getChrgMeanCd())
                    .mReqDtm(reqDtm)
                    .mTrdNo(chargeApprovalCancelRequestDto.getMTrdNo())
                    .mpsCustNo(custNo)
                    .mCustId(mCustId)
                    .trdSumry(chargeApprovalCancelRequestDto.trdSumry)
                    .custBdnFeeAmt(orgTrade.getCustBdnFeeAmt())
                    .storCd(orgTrade.getStorCd())
                    .storNm(orgTrade.getStorNm())
                    .stlMId(orgTrade.getStlMId())
                    .orgTrdDt(orgTrade.getTrdDt())
                    .orgTrdNo(orgTrade.getTrdNo())
                    .cnclYn("Y")
                    .mResrvField1(chargeApprovalCancelRequestDto.getMResrvField1())
                    .mResrvField2(chargeApprovalCancelRequestDto.getMResrvField2())
                    .mResrvField3(chargeApprovalCancelRequestDto.getMResrvField3())
                    .trdDivDtlCd(orgTrade.getTrdDivDtlCd())
                    .createdIp(ServerInfoConfig.HOST_IP)
                    .createdId(ServerInfoConfig.HOST_NAME)
                    .build()
            );

            /* 원거래 업데이트 */
            orgTrade.orgTradeUpdate(cancelMnyAmt, cancelPntAmt, 0, (curDt + curTm));
            tradeRepository.save(orgTrade);

            /* 응답 */
            return ChargeApprovalCancelResponseDto.builder()
                    .custNo(custNo)
                    .mTrdNo(chargeApprovalCancelRequestDto.getMTrdNo())
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdAmt(String.valueOf(cancelAmt))
                    .custBdnFeeAmt(String.valueOf(orgTrade.getCustBdnFeeAmt()))
                    .mnyBlc(String.valueOf(mnyBlc))
                    .pntBlc(String.valueOf(pntBlc))
                    .build();
        } else {
            TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
            tradeFailInsertDto.setTrdNo(trdNo);
            tradeFailInsertDto.setCustNo(custNo);
            tradeFailInsertDto.setMCustId(mCustId);
            tradeFailInsertDto.setFailDt(curDt);
            tradeFailInsertDto.setFailTm(curTm);
            tradeFailInsertDto.setTrdDivCd(trdDivCd);
            tradeFailInsertDto.setMid(mid);
            tradeFailInsertDto.setMTrdNo(chargeApprovalCancelRequestDto.getMTrdNo());
            tradeFailInsertDto.setAmtSign(-1);
            tradeFailInsertDto.setTrdAmt(cancelAmt);
            tradeFailInsertDto.setMnyAmt(cancelMnyAmt);
            tradeFailInsertDto.setPntAmt(cancelPntAmt);
            tradeFailInsertDto.setWaitMnyAmt(0);
            tradeFailInsertDto.setMnyBlc(mnyBlc);
            tradeFailInsertDto.setPntBlc(pntBlc);
            tradeFailInsertDto.setWaitMnyBlc((Long) payCancelOut.getOutWaitMnyBlc());
            tradeFailInsertDto.setCustBdnFeeAmt(orgTrade.getCustBdnFeeAmt());
            tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
            tradeFailInsertDto.setChrgMeanCd(orgTrade.getChrgMeanCd());
            tradeFailInsertDto.setReqDtm(reqDtm);
            tradeFailInsertDto.setOrgTrDt(orgTrade.getTrdDt());
            tradeFailInsertDto.setOrgTrNo(orgTrade.getTrdNo());
            tradeFailInsertDto.setTrdSumry(chargeApprovalCancelRequestDto.trdSumry);
            tradeFailInsertDto.setErrCd(String.valueOf(resCode));
            tradeFailInsertDto.setErrMsg(resMsg);
            tradeFailInsertDto.setPrdtCd(MpsPrdtCd.charge.getPrdtCd());
            tradeFailInsertDto.setMResrvField1(chargeApprovalCancelRequestDto.getMResrvField1());
            tradeFailInsertDto.setMResrvField2(chargeApprovalCancelRequestDto.getMResrvField2());
            tradeFailInsertDto.setMResrvField3(chargeApprovalCancelRequestDto.getMResrvField3());
            log.info("지급취소 거래실패 data : {} ", tradeFailInsertDto);
            tradeFailService.insertTradeFail(tradeFailInsertDto);
        }

        if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);

        } else if (resCode == ProcResCd.LIMIT_REACHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.CHARGE_LIMIT_REACHED);

        } else if (resCode == ProcResCd.MONTHLY_LIMIT_REACHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.MONTHLY_LIMIT_REACHED);

        } else if (resCode == ProcResCd.REQ_AMT_NOT_MATCHED.getResCd()) { //원거래금액 불일치, 취소가능금액=요청금액 불일치,
            throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);

        } else if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);

        } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd() || resCode == ProcResCd.ERROR.getResCd()) {
            log.info("거래실패: {}", resMsg);
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE);
        } else {
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE, " [충전 취소 오류]");
        }
    }

    /* 관리자 머니/포인트 지급 */
    public AdminChargeApprovalResponseDto chkAdminCharge(AdminChargeApprovalRequestDto adminChargeApprovalRequestDto) {

        List<String> procStatCdList = new ArrayList<>(Arrays.asList(AdminProcStatCd.A.getProcStatCd(), AdminProcStatCd.WAITTING.getProcStatCd()));

        AdminTrdReq tgtAdminTrdReq = adminTrdReqRepository.findByTrdReqNoAndProcStatCdInAndMid(adminChargeApprovalRequestDto.getTrdReqNo(), procStatCdList, adminChargeApprovalRequestDto.getMid());
        if (tgtAdminTrdReq == null) {
            throw new RequestValidationException(ErrorCode.ADMIN_CHARGE_ERROR, "올바르지 않은 TRD_REQ_NO 입니다.");
        } else {
            if (!TrdDivCd.MONEY_PROVIDE.getTrdDivCd().equals(tgtAdminTrdReq.getTrdDivCd()) && !TrdDivCd.POINT_PROVIDE.getTrdDivCd().equals(tgtAdminTrdReq.getTrdDivCd())
                    && !TrdDivCd.MONEY_WITHDRAW.getTrdDivCd().equals(tgtAdminTrdReq.getTrdDivCd()) && !TrdDivCd.WAITMONEY_WITHDRAW.getTrdDivCd().equals(tgtAdminTrdReq.getTrdDivCd())
                    && !TrdDivCd.POINT_REVOKE.getTrdDivCd().equals(tgtAdminTrdReq.getTrdDivCd())) {
                throw new RequestValidationException(ErrorCode.ADMIN_CHARGE_ERROR, "올바르지 않은 TRD_DIV_CD 입니다.");
            }
        }
        return AdminChargeApprovalResponseDto.builder().procStstCd(AdminProcStatCd.A.getProcStatCd()).build();
    }
}
