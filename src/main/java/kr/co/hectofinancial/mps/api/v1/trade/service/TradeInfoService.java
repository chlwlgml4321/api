package kr.co.hectofinancial.mps.api.v1.trade.service;

import kr.co.hectofinancial.mps.api.v1.trade.dto.*;
import kr.co.hectofinancial.mps.global.util.CustomerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeInfoService {
    private final TradeService tradeService;

    public List<TradeInfoListResponseDto> getTradesByCustNoAndConditions(TradeInfoListRequestDto tradeInfoListRequestDto) throws Exception {
        CustomerUtil.checkValidCustomerByCi(tradeInfoListRequestDto.getCi(), tradeInfoListRequestDto.getCustomerDto());
        return tradeService.findTradesByConditions(tradeInfoListRequestDto);
    }

    public TradeInfoResponseDto getTradeByTrdNoAndTrdDt(TradeInfoRequestDto tradeInfoRequestDto) throws Exception {
        CustomerUtil.checkValidCustomerByCi(tradeInfoRequestDto.getCi(), tradeInfoRequestDto.getCustomerDto());
        return tradeService.findTradeByTrdNoAndTrdDt(tradeInfoRequestDto);
    }

    public List<TradeInfoResponseDto> findTradesByMpsCustNoAndPeriodAndMTrdNo(TradeInfoByMarketRequestDto tradeInfoByMarketRequestDto) throws Exception {
        CustomerUtil.checkValidCustomerByCi(tradeInfoByMarketRequestDto.getCi(), tradeInfoByMarketRequestDto.getCustomerDto());
        return tradeService.findTradesByMpsCustNoAndPeriodAndMTrdNo(tradeInfoByMarketRequestDto);
    }


    public TradeUseSummaryResponseDto getTradeUseSum(TradeUseSummaryRequestDto tradeUseSummaryRequestDto) {
        CustomerUtil.checkValidCustomerByCi(tradeUseSummaryRequestDto.getCi(), tradeUseSummaryRequestDto.getCustomerDto());
        return tradeService.getTradeUseSum(tradeUseSummaryRequestDto);
    }

}
