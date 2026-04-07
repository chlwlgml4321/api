package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeUseSummaryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface TradeRepositoryCustom {
    List<Trade> findAllByMpsCustNoAndTrdDtLikeAndTrdDivCdIn(Map<String,Object> param, Pageable pageable);

    TradeUseSummaryResponseDto sumTradeUseByMpsCustNoAndTrdDtLike(Map<String, Object> param);


}
