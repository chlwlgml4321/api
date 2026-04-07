package kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto;

import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.PgPayRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.PgPayResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeFailInsertDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.api.v1.trade.service.TradeFailService;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.ApprovalService;
import kr.co.hectofinancial.mps.global.constant.MpsPrdtCd;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.error.exception.WhiteLabelException;
import kr.co.hectofinancial.mps.global.extern.whitelabel.dto.WhiteLabelPayResponseDto;
import kr.co.hectofinancial.mps.global.extern.whitelabel.service.WhiteLabelService;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgPayServiceSupport {

    private final WhiteLabelService whiteLabelService;

    private final ApprovalService approvalService;

    private final SequenceService sequenceService;

    private final TradeFailService tradeFailService;

    private final TradeRepository tradeRepository;

    /**
     * PG 화이트라벨 연동 (계좌 / 카드) + 머니 충전 프로시저
     */
    @Transactional(rollbackFor = Exception.class)
    public PgPayResponseDto payWithWhiteLabelAndChargeMoney(CustomerDto customerDto, PgPayRequestDto pgPayRequestDto) {

        String mpsCustNo = pgPayRequestDto.getMpsCustNo();
        String mTrdNo = pgPayRequestDto.getMTrdNo();
        String reqDt = pgPayRequestDto.getReqDt();
        String reqTm = pgPayRequestDto.getReqTm();
        String reqAmt = String.valueOf(pgPayRequestDto.getReqAmt());

        String blcAmt = String.valueOf(pgPayRequestDto.getBlcAmt()); //머니잔액
        String custBdnFeeAmt = String.valueOf(pgPayRequestDto.getCustBdnFeeAmt());  //고객부담수수료

        TrdChrgMeanCd chrgMeanCd = pgPayRequestDto.getChrgMeanCd(); //RP
        String chrgMeanCdStr = chrgMeanCd.getChrgMeanCd();

        log.info("[payWithWhiteLabelAndChargeMoney][START] custNo={} mTrdNo={} reqAmt={} chrgMeanCd={}", mpsCustNo, mTrdNo, reqAmt, chrgMeanCdStr);

        /* Insert 시점의 chrgTrdNo 고정 */
        String chrgTrdNo = "N";
        try {

            /* 머니 충전 Insert */
            ChargeApprovalRequestDto chargeApprovalRequestDto = ChargeApprovalRequestDto.builder()
                    .customerDto(customerDto)
                    .custNo(mpsCustNo)
                    .chrgMeanCd(chrgMeanCdStr)
                    .mTrdNo(mTrdNo)
                    .trdAmt(reqAmt)
                    .divCd(pgPayRequestDto.getTrdDivCd())
                    .blcAmt(blcAmt)
                    .reqDt(reqDt)
                    .reqTm(reqTm)
                    .trdSumry(pgPayRequestDto.getTrdSumry())
                    .custBdnFeeAmt(custBdnFeeAmt)
                    .chrgTrdNo(chrgTrdNo)
                    .trdDivDtlCd(pgPayRequestDto.getTrdDivDtlCd())
                    .mResrvField1(pgPayRequestDto.getMResrvField1())
                    .mResrvField2(pgPayRequestDto.getMResrvField2())
                    .mResrvField3(pgPayRequestDto.getMResrvField3())
                    .build();

            ChargeApprovalResponseDto chargeApprovalResponseDto = approvalService.chargeApproval(chargeApprovalRequestDto);
            String trdNo = chargeApprovalResponseDto.getTrdNo();
            String trdDt = chargeApprovalResponseDto.getTrdDt();
            String trdTm = chargeApprovalResponseDto.getTrdTm();
            String mnyBlc = chargeApprovalResponseDto.getMnyBlc();
            log.info("[payWithWhiteLabelAndChargeMoney][머니충전 SUCCESS] custNo={} mTrdNo={} reqAmt={} trdNo={} mnyBlc={}", mpsCustNo, mTrdNo, reqAmt, trdNo, mnyBlc);

            /* 화이트라벨연동 연동 */
            WhiteLabelPayResponseDto.Params params = whiteLabelService.processPayment(pgPayRequestDto);
            chrgTrdNo = params.getTrdNo();
            String chrgDtm = params.getTrdDt() + params.getTrdTm();
            log.info("[payWithWhiteLabelAndChargeMoney][PG연동 SUCCESS] custNo={} mTrdNo={} trdNo={} chrgTrdNo={}", mpsCustNo, mTrdNo, trdNo, chrgTrdNo);

            return PgPayResponseDto.builder()
                    .resultCode("0000")
                    .resultMsg("성공")
                    .trdNo(trdNo)
                    .trdDt(trdDt)
                    .trdTm(trdTm)
                    .chrgTrdNo(chrgTrdNo)
                    .chrgDtm(chrgDtm)
                    .mTrdNo(mTrdNo)
                    .reqDt(reqDt)
                    .reqTm(reqTm)
                    .reqAmt(Long.parseLong(reqAmt))
                    .preMnyBlc(Long.parseLong(blcAmt))
                    .postMnyBlc(Long.parseLong(mnyBlc))
                    .bankCd(pgPayRequestDto.getBankCd())
                    .custAcntKey(pgPayRequestDto.getCustAcntKey())
                    .custAcntSuffix(pgPayRequestDto.getCustAcntSuffix())
                    .fnCd(pgPayRequestDto.getFnCd())
                    .cardKey(pgPayRequestDto.getCardKey())
                    .cardSuffix(pgPayRequestDto.getCardSuffix())
                    .encKey(pgPayRequestDto.getEncKey())
                    .pktHashKey(pgPayRequestDto.getPktHashKey())
                    .mid(pgPayRequestDto.getMid())
                    .mpsCustNo(mpsCustNo)
                    .mCustId(pgPayRequestDto.getMCustId())
                    .mchtCustId(pgPayRequestDto.getMchtCustId())
                    .tryCnt(pgPayRequestDto.getTryCnt())
                    .autoChargeType(pgPayRequestDto.getAutoChargeType())
                    .chrgMeanCd(chrgMeanCd)
                    .build();

        } catch (RequestValidationException e) {
            throw e;
        } catch (WhiteLabelException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * MPS.PM_MPS_TRD 에 CHRG_TRD_NO (PG거래번호) UPDATE
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateChrgTrdNo(PgPayResponseDto pgPayResponseDto) {

        String chrgTrdNo = pgPayResponseDto.getChrgTrdNo();
        String trdNo = pgPayResponseDto.getTrdNo();
        String trdDt = pgPayResponseDto.getTrdDt();
        String mpsCustNo = pgPayResponseDto.getMpsCustNo();
        String mTrdNo = pgPayResponseDto.getMTrdNo();
        log.info("[updateChrgTrdNo][START] custNo={} trdNo={} trdDt={} chrgTrdNo={} mTrdNo={}", mpsCustNo, trdNo, trdDt, chrgTrdNo, mTrdNo);

        int updateCnt = tradeRepository.updateChrgTrdNoAndRmkByTrdNoAndTrdDtAndCustNo(chrgTrdNo, trdNo, trdDt);
        if (updateCnt == 1) {
            log.info("[updateChrgTrdNo][SUCCESS] custNo={} trdNo={} trdDt={} chrgTrdNo={} mTrdNo={}", mpsCustNo, trdNo, trdDt, chrgTrdNo, mTrdNo);
            return;
        }

        /* MTMS */
        String msg = MessageFormatter
                .arrayFormat("custNo={} trdNo={} trdDt={} chrgTrdNo={} mTrdNo={}", new Object[]{mpsCustNo, trdNo, trdDt, chrgTrdNo, mTrdNo})
                .getMessage();
        MonitAgent.sendMonitAgent(ErrorCode.DB_UPDATE_FAIL.getErrorCode(), (ErrorCode.DB_UPDATE_FAIL.getErrorMessage() + msg));
        log.error("[updateChrgTrdNo][FAIL: updated rows={}] custNo={} trdNo={} trdDt={} chrgTrdNo={} mTrdNo={}", updateCnt, mpsCustNo, trdNo, trdDt, chrgTrdNo, mTrdNo);
    }

    /**
     * PG 거래 실패시 거래실패건 MPS.PM_MPS_TRD_FAIL 에 저장
     */
    public void insertTradeFail(PgPayRequestDto pgPayRequestDto, String errCd, String errMsg) {
        /* trdNo 채번*/
        String trdNo = sequenceService.generateTradeSeq01();
        String chrgMeanCd = pgPayRequestDto.getChrgMeanCd().getChrgMeanCd();
        String divCd = pgPayRequestDto.getTrdDivCd();
        String blcUseOrd = pgPayRequestDto.getBlcUseOrd();

        /* 실패 시각 */
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();

        String mTrdNo = pgPayRequestDto.getMTrdNo();
        String mpsCustNo = pgPayRequestDto.getMpsCustNo();

        log.info("[insertTradeFail][START] custNo={} trdNo={} trdDt={} mTrdNo={}", mpsCustNo, trdNo, curDt, mTrdNo);
        TradeFailInsertDto trdFailDto = TradeFailInsertDto.builder()
                .trdNo(trdNo)
                .custNo(mpsCustNo)
                .mid(pgPayRequestDto.getMid())
                .chrgTrdNo(WhiteLabelService.getWhiteLabelMTrdNo(mTrdNo, pgPayRequestDto.getAutoChargeType().getValue(), pgPayRequestDto.getTryCnt()))
                .failDt(curDt)
                .failTm(curTm)
                .trdDivCd(divCd)
                .amtSign(+1)//충전
                .trdAmt(pgPayRequestDto.getReqAmt())
                .mnyAmt(pgPayRequestDto.getReqAmt())
                .pntAmt(0L)
                .waitMnyAmt(0L)
                .mnyBlc(pgPayRequestDto.getBlcAmt())
                .pntBlc(0L)
                .waitMnyBlc(0L)
                .custBdnFeeAmt(pgPayRequestDto.getCustBdnFeeAmt())
                .blcUseOrd(blcUseOrd)
                .chrgMeanCd(chrgMeanCd)
                .reqDtm((pgPayRequestDto.getReqDt() + pgPayRequestDto.getReqTm()))
                .mTrdNo(mTrdNo)
                .trdSumry(pgPayRequestDto.getTrdSumry())
                .errCd(errCd)
                .errMsg(errMsg)
                .csrcIssReqYn("N")
                .prdtCd(MpsPrdtCd.charge.getPrdtCd())
                .mCustId(pgPayRequestDto.getMCustId())
                .trdDivDtlCd(pgPayRequestDto.getTrdDivDtlCd())
                .build();
        tradeFailService.insertTradeFail(trdFailDto);
        log.info("[insertTradeFail][SUCCESS] custNo={} trdNo={} trdDt={} mTrdNo={}", mpsCustNo, trdNo, curDt, mTrdNo);
    }

}