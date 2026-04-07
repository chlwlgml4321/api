package kr.co.hectofinancial.mps.api.v1.trade.service;

import kr.co.hectofinancial.mps.api.v1.trade.domain.TrdFail;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeFailInsertDto;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TrdFailRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.MpsApiCd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeFailService {
    private final TrdFailRepository trdFailRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertTradeFail(TradeFailInsertDto tradeFailInsertDto) {
        trdFailRepository.save(TrdFail.builder()
                .trdNo(tradeFailInsertDto.getTrdNo())
                .mpsCustNo(tradeFailInsertDto.getCustNo())
                .mCustId(tradeFailInsertDto.getMCustId())
                .failDt(tradeFailInsertDto.getFailDt())
                .failTm(tradeFailInsertDto.getFailTm())
                .trdDivCd(tradeFailInsertDto.getTrdDivCd())
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(tradeFailInsertDto.getPrdtCd())
                .mid(tradeFailInsertDto.getMid())
                .mTrdNo(tradeFailInsertDto.getMTrdNo())
                .amtSign(tradeFailInsertDto.getAmtSign())
                .trdAmt(tradeFailInsertDto.getTrdAmt())
                .mnyAmt(tradeFailInsertDto.getMnyAmt())
                .pntAmt(tradeFailInsertDto.getPntAmt())
                .waitMnyAmt(tradeFailInsertDto.getWaitMnyAmt())
                .mnyBlc(tradeFailInsertDto.getMnyBlc())
                .pntBlc(tradeFailInsertDto.getPntBlc())
                .waitMnyBlc(tradeFailInsertDto.getWaitMnyBlc())
                .custBdnFeeAmt(tradeFailInsertDto.getCustBdnFeeAmt())
                .trdSumry(tradeFailInsertDto.getTrdSumry())
                .blcUseOrd(tradeFailInsertDto.getBlcUseOrd())
                .chrgMeanCd(tradeFailInsertDto.getChrgMeanCd())
                .mReqDtm(tradeFailInsertDto.getReqDtm())
                .mTrdNo(tradeFailInsertDto.getMTrdNo())
                .trdSumry(tradeFailInsertDto.getTrdSumry())
                .orgTrdDt(tradeFailInsertDto.getOrgTrDt())
                .orgTrdNo(tradeFailInsertDto.getOrgTrNo())
                .errCd(tradeFailInsertDto.getErrCd())
                .errMsg(tradeFailInsertDto.getErrMsg())
                .createdIp(ServerInfoConfig.HOST_IP)
                .createdId(ServerInfoConfig.HOST_NAME)
                .stlMId(tradeFailInsertDto.getStlMId())
                .csrcIssReqYn(tradeFailInsertDto.getCsrcIssReqYn())
                .csrcIssStatCd(tradeFailInsertDto.getCsrcIssStatCd())
                .storCd(tradeFailInsertDto.getStorCd())
                .storNm(tradeFailInsertDto.getStorNm())
                .chrgTrdNo(tradeFailInsertDto.getChrgTrdNo())
                .rcvMpsCustNo(tradeFailInsertDto.getRcvMpsCustNo())
                .trdDivDtlCd(tradeFailInsertDto.getTrdDivDtlCd())
                .mResrvField1(tradeFailInsertDto.getMResrvField1())
                .mResrvField2(tradeFailInsertDto.getMResrvField2())
                .mResrvField3(tradeFailInsertDto.getMResrvField3())
                .cardMngNo(tradeFailInsertDto.getCardMngNo())
                .gcReqPkt(tradeFailInsertDto.getGcPktReq())
                .build());
    }
}
