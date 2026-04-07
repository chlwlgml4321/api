package kr.co.hectofinancial.mps.api.v1.trade.service.wallet;

import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeFailInsertDto;
import kr.co.hectofinancial.mps.api.v1.trade.service.TradeFailService;
import kr.co.hectofinancial.mps.global.constant.MpsPrdtCd;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UseFailService {
    private final SequenceService sequenceService;
    private final TradeFailService tradeFailService;

    /**
     * 부족분 자동충전 실패 이후, 사용실패 거래 쌓는 메서드 (불필요하게 사용프로시저 태우지않고 거래실패 쌓기 위해)
     * @param tradeFailInsertDto
     * @param customerDto
     */
    public void createUseTradeFail(TradeFailInsertDto tradeFailInsertDto, CustomerDto customerDto) {
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        log.info("[createUseTradeFail] custNo={} 부족분 자동충전 실패로 거래실패 insert 시작!", customerDto.getMpsCustNo());

        tradeFailInsertDto.setMid(customerDto.getMid());
        tradeFailInsertDto.setCustNo(customerDto.getMpsCustNo());
        tradeFailInsertDto.setMCustId(customerDto.getMCustId());
        tradeFailInsertDto.setTrdNo(sequenceService.generateTradeSeq01());
        tradeFailInsertDto.setTrdDivCd(TrdDivCd.COMMON_USE.getTrdDivCd());
        tradeFailInsertDto.setFailDt(customDateTimeUtil.getDate());
        tradeFailInsertDto.setFailTm(customDateTimeUtil.getTime());
        tradeFailInsertDto.setAmtSign(-1);
        tradeFailInsertDto.setErrCd(ErrorCode.REQ_AMT_NOT_MATCHED.getErrorCode());
        tradeFailInsertDto.setErrMsg(ErrorCode.REQ_AMT_NOT_MATCHED.getErrorMessage());
        tradeFailInsertDto.setCsrcIssStatCd("N");
        tradeFailInsertDto.setPrdtCd(MpsPrdtCd.use.getPrdtCd());
        tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
        tradeFailService.insertTradeFail(tradeFailInsertDto);

        log.info("[createUseTradeFail] custNo={} 부족분 자동충전 실패로 거래실패 insert 성공!", customerDto.getMpsCustNo());

    }
}
