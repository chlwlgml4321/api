package kr.co.hectofinancial.mps.api.v1.trade.service;

import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.market.repository.MpsMarketRepository;
import kr.co.hectofinancial.mps.api.v1.authentication.service.AuthenticationService;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.dto.*;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.*;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.CustomerWalletResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.PntExpr.PntExprIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.PntExpr.PntExprOut;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseEach.UseEachIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseEach.UseEachOut;
import kr.co.hectofinancial.mps.api.v1.trade.repository.CustWlltRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.PayPntRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.api.v1.trade.service.wallet.WalletService;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {

    private final SequenceService sequenceService;
    private final CustWlltRepository custWlltRepository;
    private final TradeFailService tradeFailService;
    private final TradeRepository tradeRepository;
    private final PayPntRepository payPntRepository;
    private final AuthenticationService authenticationService;
    private final MpsMarketRepository mpsMarketRepository;
    private final WalletService walletService;
    @Transactional(rollbackFor = Exception.class)
    public PointExpireResponseDto pointExpire(PointExpireRequestDto pointExpireRequestDto) {

        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();

        String pntExpTrdNo = sequenceService.generateTradeSeq01();

        String expTrDTm = pointExpireRequestDto.getReqDt() + pointExpireRequestDto.getReqTm();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime localDateTime = LocalDateTime.parse(expTrDTm, formatter);
        LocalDateTime customDate = localDateTime.plusSeconds(2);
        expTrDTm = customDate.format(formatter);
        String expTrdDt = expTrDTm.substring(0,8);
        String expTrdTm = expTrDTm.substring(8, 14);

        /* 원거래조회 */
        Trade orgTrade = tradeRepository.findByTrdNoAndTrdDt(pointExpireRequestDto.getOrgTrdNo(), pointExpireRequestDto.getOrgTrdDt());

        /* 포인트 만료 프로시저 호출 */
        PntExprIn pntExprIn = PntExprIn.builder()
                .inMpsCustNo(pointExpireRequestDto.custNo)
                .inTrdDivCd(TrdDivCd.POINT_EXPIRE.getTrdDivCd())
                .inUseTrdNo(pntExpTrdNo)
                .inUseTrdDt(pointExpireRequestDto.getReqDt())
                .inCnclTrdNo(pointExpireRequestDto.getCnclTrdNo())
                .inCnclTrdDt(pointExpireRequestDto.getCnclTrdDt())
                .inTrdAmt(pointExpireRequestDto.getExpPntAmt())
                .inWorkerID(ServerInfoConfig.HOST_NAME)
                .inWorkerIP(ServerInfoConfig.HOST_IP)
                .build();
        PntExprOut pntExprOut = custWlltRepository.pointExpr(pntExprIn);
        long exprResCode = (long) pntExprOut.getOutResCd();
        String ExprResMsg = pntExprOut.getOutResMsg();
        log.info("응답코드: [{}] 응답메시지: [{}] 고객잔액: [{}]", exprResCode, ExprResMsg, pntExprOut);

        if (exprResCode != ProcResCd.SUCCESS.getResCd()) {
            log.error("**[사용취소] 포인트 만료 프로시저 응답 에러** 회원번호: [{}], 입력금액: [{}], 조회금액: [{}]", pointExpireRequestDto.custNo, pointExpireRequestDto.getExpPntAmt(), pntExprOut.getOutExprPntAmt());

            String message = String.format("**ERROR 발생** 포인트 만료 / [선불회원번호: " + pointExpireRequestDto.custNo + ", 사용취소거래번호: " + pointExpireRequestDto.getCnclTrdNo() + ", 응답메시지: " + ExprResMsg + "]");
            MonitAgent.sendMonitAgent(ErrorCode.POINT_EXPIRE_FAIL.getErrorCode(), message);

            TradeFailInsertDto trdFailDto = TradeFailInsertDto.builder()
                    .trdNo(pntExpTrdNo)
                    .failDt(curDt)
                    .failTm(curTm)
                    .trdDivCd(TrdDivCd.POINT_EXPIRE.getTrdDivCd())
                    .mid(pointExpireRequestDto.getCustomerDto().getMid())
                    .custNo(pointExpireRequestDto.getCustNo())
                    .mCustId(pointExpireRequestDto.getCustomerDto().getMCustId())
                    .amtSign(-1)
                    .trdAmt(pointExpireRequestDto.getExpPntAmt())
                    .mnyAmt(0)
                    .pntAmt(pointExpireRequestDto.getExpPntAmt())
                    .mnyBlc((Long)pntExprOut.getOutMnyBlc())
                    .pntBlc((Long)pntExprOut.getOutPntBlc())
                    .reqDtm(expTrDTm)
                    .mTrdNo(pointExpireRequestDto.getCnclTrdNo())
                    .trdSumry("[사용취소] 포인트만료")
                    .orgTrDt(orgTrade.getTrdDt())
                    .orgTrNo(orgTrade.getTrdNo())
                    .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                    .csrcIssReqYn(orgTrade.getCsrcIssReqYn())
                    .csrcIssStatCd(orgTrade.getCsrcIssStatCd())
                    .storNm(orgTrade.getStorNm())
                    .storCd(orgTrade.getStorCd())
                    .stlMId(orgTrade.getStlMId())
                    .errCd(String.valueOf(exprResCode))
                    .errMsg(ExprResMsg)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .build();

            //실패 => PM_MPS_TRD_FAIL 테이블
            tradeFailService.insertTradeFail(trdFailDto);
            throw new RequestValidationException(ErrorCode.POINT_EXPIRE_ERROR);

        } else if (exprResCode == ProcResCd.SUCCESS.getResCd()) {
            /* 포인트 만료 */
            tradeRepository.save(Trade.builder()
                    .trdNo(pntExpTrdNo)
                    .trdDt(expTrdDt)
                    .trdTm(expTrdTm)
                    .trdDivCd(TrdDivCd.POINT_EXPIRE.getTrdDivCd())
                    .svcCd(MpsApiCd.SVC_CD)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .mid(pointExpireRequestDto.getCustomerDto().getMid())
                    .amtSign(-1)
                    .trdAmt((Long)pntExprOut.getOutExprPntAmt())
                    .mnyAmt(0L)
                    .pntAmt((Long)pntExprOut.getOutExprPntAmt())
                    .waitMnyAmt(0L)
                    .mnyBlc((Long) pntExprOut.getOutMnyBlc())
                    .pntBlc((Long) pntExprOut.getOutPntBlc())
                    .waitMnyBlc((Long) pntExprOut.getOutWaitMnyBlc())
                    .blcUseOrd("P")
                    .chrgMeanCd("P")
                    .mReqDtm(expTrDTm)
                    .mTrdNo(pointExpireRequestDto.getCnclTrdNo())
                    .mpsCustNo(pointExpireRequestDto.custNo)
                    .mCustId(pointExpireRequestDto.getCustomerDto().getMCustId())
                    .createdIp(ServerInfoConfig.HOST_IP)
                    .createdId(ServerInfoConfig.HOST_NAME)
                    .stlMId(orgTrade.getStlMId())
                    .storCd("N")
                    .storNm("N")
                    .build()
            );
        }

        return PointExpireResponseDto.builder()
                .expPntAmt((Long)pntExprOut.getOutExprPntAmt())
                .pntBlc((Long)pntExprOut.getOutPntBlc())
                .expTrdDt(expTrdDt)
                .expTrdTm(expTrdTm)
                .build();
    }

    /* 포인트 만료 조회 (회원별 60일) */
    public GetExpPntByCustNoResponseDto getExpPntByCustNo(GetExpPntByCustNoRequestDto getExpPntByCustNoRequestDto){

        /* 일자 시각 SET */
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();

        String maxExpDt = DateTimeUtil.addDate(curDt, 60);
        if (Long.parseLong(getExpPntByCustNoRequestDto.getTargetDate()) < Long.parseLong(curDt) || Long.parseLong(getExpPntByCustNoRequestDto.getTargetDate()) > Long.parseLong(maxExpDt)) {
            throw new RequestValidationException(ErrorCode.POINT_EXPIRE_DATE_ERROR);
        }

        Long willExpPntAmount;

        willExpPntAmount = payPntRepository.getExpPntTotal(getExpPntByCustNoRequestDto.getCustNo(), curDt, getExpPntByCustNoRequestDto.getTargetDate());
        willExpPntAmount = (willExpPntAmount == null ? 0 : willExpPntAmount);

        return GetExpPntByCustNoResponseDto.builder()
                .custNo(getExpPntByCustNoRequestDto.getCustNo())
                .expPntBlc(String.valueOf(willExpPntAmount))
                .build();

    }

    /* 포인트 회수 */
    public PointRevokeResponseDto pointRevoke(PointRevokeRequestDto pointRevokeRequestDto){

        /* 일자 시각 SET */
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();
        String mReqDtm = curDt + curTm;
        if (StringUtils.isNotEmpty(pointRevokeRequestDto.getReqDt()) && StringUtils.isNotEmpty(pointRevokeRequestDto.getReqTm())) {
            mReqDtm = pointRevokeRequestDto.getReqDt() + pointRevokeRequestDto.getReqTm();
        }

        //회원 추출
        CustomerDto customerDto = pointRevokeRequestDto.getCustomerDto();
        String mCustId = customerDto.getMCustId();
        String mid = customerDto.getMid();

        String trdNo = sequenceService.generateTradeSeq01();
        String blcUseOrd = TrBlcUseOrd.POINT.getBlcUseOrd();
        String trdDivCd = TrdDivCd.POINT_REVOKE.getTrdDivCd();
        long trdAmt = Long.parseLong(pointRevokeRequestDto.getTrdAmt());

        //금액검증
        if (CommonUtil.nullTrim(pointRevokeRequestDto.getTrdAmt()).equals("0")) {
            throw new RequestValidationException(ErrorCode.TRADE_AMT_ERROR);
        }

        /* 회원 잔액조회 */
        CustomerWalletResponseDto custWallet = walletService.getCustWalletByCustNo(customerDto.getMpsCustNo());
        Long pntAmt = custWallet.getPntBlc();

        if (pntAmt < Long.parseLong(pointRevokeRequestDto.getTrdAmt())) {
            throw new RequestValidationException(ErrorCode.POINT_REVOKE_AMT_ERROR);
        }

        /* 포인트회수(사용) 프로시저 호출 */
        UseEachIn useEachInParam = UseEachIn.builder()
                .inMpsCustNo(customerDto.getMpsCustNo())
                .inTrdDivCd(TrdDivCd.COMMON_USE.getTrdDivCd())
                .inUseTrdNo(trdNo)
                .inUseTrdDt(curDt)
                .inMnyAmt(0L)
                .inPntAmt(trdAmt)
                .inBlc(Long.parseLong(pointRevokeRequestDto.getPntBlc()) + custWallet.getMnyBlc())
                .inWorkerID(ServerInfoConfig.HOST_NAME)
                .inWorkerIP(ServerInfoConfig.HOST_IP)
                .build();
        UseEachOut useEachOut = tradeRepository.useEach(useEachInParam);

        long resCode = (long) useEachOut.getOutResCd();
        String resMsg = useEachOut.getOutResMsg();
        log.info("응답코드: [{}] 응답메시지: [{}]", resCode, resMsg);

        if (resCode == ProcResCd.SUCCESS.getResCd()) {
            //정상거래 set
            tradeRepository.save(Trade.builder()
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdDivCd(trdDivCd)
                    .svcCd(MpsApiCd.SVC_CD)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .mid(mid)
                    .amtSign(-1)
                    .trdAmt(trdAmt)
                    .mnyAmt(0)
                    .pntAmt(trdAmt)
                    .waitMnyAmt(0)
                    .mnyBlc(useEachOut.getOutMnyBlc())
                    .pntBlc(useEachOut.getOutPntBlc())
                    .waitMnyBlc(useEachOut.getOutWaitMnyBlc())
                    .custBdnFeeAmt(0)
                    .blcUseOrd(blcUseOrd)
                    .chrgMeanCd(TrdChrgMeanCd.POINT_RELATED.getChrgMeanCd())//충전수단 코드
                    .mReqDtm(mReqDtm)
                    .mTrdNo(pointRevokeRequestDto.getMTrdNo())
                    .mpsCustNo(customerDto.getMpsCustNo())
                    .trdSumry(pointRevokeRequestDto.getTrdSumry())
                    .stlMId(mid)
                    .storCd("N")
                    .storNm("N")
                    .mCustId(mCustId)
                    .mResrvField1(pointRevokeRequestDto.getMResrvField1())
                    .mResrvField2(pointRevokeRequestDto.getMResrvField2())
                    .mResrvField3(pointRevokeRequestDto.getMResrvField3())
                    .createdIp(ServerInfoConfig.HOST_IP)
                    .createdId(ServerInfoConfig.HOST_NAME)
                    .build()
            );

            return PointRevokeResponseDto.builder()
                    .custNo(customerDto.getMpsCustNo())
                    .mTrdNo(pointRevokeRequestDto.getMTrdNo())
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdAmt(String.valueOf(useEachOut.getOutPntAmt()))
                    .pntBlc(String.valueOf(useEachOut.getOutPntBlc()))
                    .build();

        } else {
            TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
            tradeFailInsertDto.setTrdNo(trdNo);
            tradeFailInsertDto.setCustNo(customerDto.getMpsCustNo());
            tradeFailInsertDto.setFailDt(curDt);
            tradeFailInsertDto.setFailTm(curTm);
            tradeFailInsertDto.setTrdDivCd(trdDivCd);
            tradeFailInsertDto.setMid(mid);
            tradeFailInsertDto.setMCustId(mCustId);
            tradeFailInsertDto.setMTrdNo(pointRevokeRequestDto.getMTrdNo());
            tradeFailInsertDto.setAmtSign(-1);
            tradeFailInsertDto.setTrdAmt(trdAmt);
            tradeFailInsertDto.setMnyAmt(0);
            tradeFailInsertDto.setPntAmt(trdAmt);
            tradeFailInsertDto.setCustBdnFeeAmt(0);
            tradeFailInsertDto.setWaitMnyAmt(0);
            tradeFailInsertDto.setMnyBlc(useEachOut.getOutMnyBlc());
            tradeFailInsertDto.setPntBlc(useEachOut.getOutPntBlc());
            tradeFailInsertDto.setWaitMnyBlc(useEachOut.getOutWaitMnyBlc());
            tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
            tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.POINT_RELATED.getChrgMeanCd());
            tradeFailInsertDto.setReqDtm(mReqDtm);
            tradeFailInsertDto.setTrdSumry(pointRevokeRequestDto.getTrdSumry());
            tradeFailInsertDto.setErrCd(String.valueOf(resCode));
            tradeFailInsertDto.setErrMsg(resMsg);
            tradeFailInsertDto.setPrdtCd(MpsPrdtCd.use.getPrdtCd());
            log.info("포인트 회수 거래실패 data : {} ", tradeFailInsertDto);
            tradeFailService.insertTradeFail(tradeFailInsertDto);
        }

        if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);

        } else if (resCode == ProcResCd.REQ_AMT_NOT_MATCHED.getResCd()) { //요청금액 > 잔액
            throw new RequestValidationException(ErrorCode.REQ_AMT_NOT_MATCHED);

        } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd() || resCode == ProcResCd.ERROR.getResCd()) {
            log.info("거래실패: {}", resMsg);
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE);
        }else{
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE);
        }
    }

    /* 포인트 만료 조회 (mid 별 60일) */
    public GetExpPntByMidResponseDto getExpPntByMid(GetExpPntByMidRequestDto getExpPntByMidRequestDto){

        GetExpPntByMidResponseDto getExpPntByMidResponseDto = new GetExpPntByMidResponseDto();
        List<Object[]> expPntList = payPntRepository.getExpPntAmtByMid(getExpPntByMidRequestDto.getMid(), getExpPntByMidRequestDto.getTargetDate());

        /* 일자 시각 SET */
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();

        String maxExpDt = DateTimeUtil.addDate(curDt, 60);
        if (Long.parseLong(getExpPntByMidRequestDto.getTargetDate()) < Long.parseLong(curDt) || Long.parseLong(getExpPntByMidRequestDto.getTargetDate()) > Long.parseLong(maxExpDt)) {
            throw new RequestValidationException(ErrorCode.POINT_EXPIRE_DATE_ERROR);
        }

        getExpPntByMidResponseDto.setTotCnt(expPntList.size());
        if (expPntList.size() > 0) {
            log.info("M_ID :[{}], 소멸예정 건수 :[{}]", getExpPntByMidRequestDto.getMid(), expPntList.size());

            for (Object[] row : expPntList) {
                String custNo = (String) row[0];
                String custId = (String) row[1];
                String vldPd = (String) row[2];
                BigDecimal exPntAmt = (BigDecimal) row[3];
                GetExpPntByMidResponseDto.PntExprList pntExprList = GetExpPntByMidResponseDto.PntExprList.builder()
                        .custNo(custNo)
                        .custId(custId)
                        .vldPd(vldPd)
                        .expPntAmt(String.valueOf(exPntAmt))
                        .build();
                getExpPntByMidResponseDto.getData().add(pntExprList);
            }
        }
        return getExpPntByMidResponseDto;
    }
}