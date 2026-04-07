package kr.co.hectofinancial.mps.api.v1.trade.service;

import kr.co.hectofinancial.mps.api.v1.trade.domain.PayPnt;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.dto.*;
import kr.co.hectofinancial.mps.api.v1.trade.repository.PayPntRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class TradeService {
    private final TradeRepository tradeRepository;
    private final PayPntRepository payPntRepository;

    /**
     * 거래내역조회
     * @param tradeInfoListRequestDto
     * @return
     */
    public List<TradeInfoListResponseDto> findTradesByConditions(TradeInfoListRequestDto tradeInfoListRequestDto) {
        Map<String, Object> param = new HashMap<>();
        /*검증*/
        String cardTrdOnlyYn = StringUtils.isBlank(CommonUtil.nullTrim(tradeInfoListRequestDto.getCardTrdOnlyYn())) ? "N" : tradeInfoListRequestDto.getCardTrdOnlyYn().toUpperCase();
        if (!"N".equals(cardTrdOnlyYn) && !"Y".equals(cardTrdOnlyYn)) {
            throw new RequestValidationException(ErrorCode.YN_PARAMETER_ERROR, "cardTrdOnlyYn(카드거래 전용 조회 여부)");
        }
        String showCnclYn = StringUtils.isBlank(tradeInfoListRequestDto.getShowCnclYn()) ? "N" : tradeInfoListRequestDto.getShowCnclYn().toUpperCase();
        if (!"N".equals(showCnclYn) && !"Y".equals(showCnclYn)) {
            throw new RequestValidationException(ErrorCode.YN_PARAMETER_ERROR, "showCnclYn(취소거래 포함 여부)");
        }
        if (StringUtils.isNotBlank(tradeInfoListRequestDto.getBlcDivCd())) {
            String blcDivCd = tradeInfoListRequestDto.getBlcDivCd().toUpperCase();
            if (!"M".equals(blcDivCd) && !"P".equals(blcDivCd)) {
                throw new RequestValidationException(ErrorCode.BLC_DIV_CD_ERROR);
            }
        }

        List<String> trdDivCds = new ArrayList<>();
        if (StringUtils.isNotBlank(tradeInfoListRequestDto.getTrdDivCd())) {
            //, 로 자른다음, 공백제거, trim, 중복제거, 유효한 trdDivCd인지 확인
            trdDivCds = Arrays.stream(tradeInfoListRequestDto.getTrdDivCd().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty() && TrdDivCd.isValid(s))
                                .distinct()
                                .collect(Collectors.toList());
        }


        Pageable pageable = PageRequest.of(tradeInfoListRequestDto.getPage() - 1, tradeInfoListRequestDto.getSize(), Sort.by(Sort.Order.desc("trdDt"), Sort.Order.desc("trdTm")));

        /*조회 조건 map 형태 parameter에 담아서 tradeRepositoryImpl 호출*/
        param.put("mpsCustNo", tradeInfoListRequestDto.getCustNo());
        param.put("trdDt", tradeInfoListRequestDto.getPeriod());
        param.put("mTrdNo", tradeInfoListRequestDto.getMTrdNo());
        param.put("trdNo", tradeInfoListRequestDto.getTrdNo());
        param.put("blcDivCd", tradeInfoListRequestDto.getBlcDivCd());
        param.put("cardTrdOnlyYn", cardTrdOnlyYn);

        if (trdDivCds.size() == 0 && "N".equals(showCnclYn.toUpperCase())) {
            //조회요청 trdDivCd 없으면서 취소거래미포함 => 취소거래건 제외 모두 조회
            param.put("trdDivCds", TrdDivCd.getNonCancelCodes());
        } else {
            param.put("trdDivCds", trdDivCds);
        }

        List<Trade> tradeList = tradeRepository.findAllByMpsCustNoAndTrdDtLikeAndTrdDivCdIn(param, pageable);
        List<TradeInfoListResponseDto> trades = tradeList.stream().map(trade -> TradeInfoListResponseDto.of(trade)).collect(Collectors.toList());
        return trades;
    }

    /**
     * 거래번호, 거래일자로 거래상세조회
     * @param tradeInfoRequestDto
     * @return
     */
    public TradeInfoResponseDto findTradeByTrdNoAndTrdDt(TradeInfoRequestDto tradeInfoRequestDto) {
        Trade findTrade = tradeRepository.findTradeByMpsCustNoAndTrdNoAndTrdDt(tradeInfoRequestDto.getCustNo(), tradeInfoRequestDto.getTrdNo(), tradeInfoRequestDto.getTrdDt()).orElseThrow(() ->
                new RequestValidationException(ErrorCode.TRADE_INFO_NOT_FOUND)
        );
        return getTradeInfoResponseDto(findTrade);
    }

    private TradeInfoResponseDto getTradeInfoResponseDto(Trade findTrade) {
        if (findTrade.getTrdDivCd().equals(TrdDivCd.POINT_PROVIDE.getTrdDivCd())) {
            //포인트충전일경우, 포인트 만료일자
            PayPnt payPnt = payPntRepository.findPayPntByPayTrdNoAndPayTrdDtAndMakeDt(findTrade.getTrdNo(), findTrade.getTrdDt(), findTrade.getTrdDt()).orElseThrow(() ->
                    new RequestValidationException(ErrorCode.PAY_POINT_NOT_FOUND));
            return TradeInfoResponseDto.of(findTrade, payPnt.getVldPd());
        }else if(findTrade.getTrdDivCd().equals(TrdDivCd.MONEY_PROVIDE.getTrdDivCd())){
            //머니충전일경우, 머니 만료일자
            Calendar cal = Calendar.getInstance();
            try {
                Date date = new SimpleDateFormat("yyyyMMdd").parse(findTrade.getTrdDt());
                cal.setTime(date);
                cal.add(Calendar.YEAR, 10); //머니 유효기간 10년
                String mnyVldPd = DateTimeUtil.convertDateToString(cal.getTime());
                return TradeInfoResponseDto.of(findTrade, mnyVldPd);
            } catch (Exception e) {
                throw new RequestValidationException(ErrorCode.DATE_FORMAT_CONVERSION_ERROR);
            }
        }
        return TradeInfoResponseDto.of(findTrade, "");
    }

    public List<TradeInfoResponseDto> findTradesByMpsCustNoAndPeriodAndMTrdNo(TradeInfoByMarketRequestDto tradeInfoByMarketRequestDto) {
        String period = tradeInfoByMarketRequestDto.getPeriod();
        String stDate = period + "01";//시작일
        String edDate = period + "31";//종료일 (31일보다 큰 마지막날은 없음)
        List<Trade> trades = tradeRepository.findTradesByMpsCustNoAndPeriodAndMTrdNo(tradeInfoByMarketRequestDto.getCustNo(), stDate, edDate, tradeInfoByMarketRequestDto.getMTrdNo());
        return trades.stream().map(trade -> getTradeInfoResponseDto(trade)).collect(Collectors.toList());
    }

    public Long findByTrDivCdCount(TradeFindByTrDivCdCountRequestDto tradeFindByTrDivCdCountRequestDto){

        Long totCnt = 0L;

        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String stDate = tradeFindByTrDivCdCountRequestDto.getTargetMonth() + "01";
        totCnt = tradeRepository.countByMpsCustNoAndTrdDivCdAndTrdDtBetween(tradeFindByTrDivCdCountRequestDto.getCustNo(), tradeFindByTrDivCdCountRequestDto.getDivCd(), stDate, curDt);

        return totCnt;
    }

    public TradeUseSummaryResponseDto getTradeUseSum(TradeUseSummaryRequestDto tradeUseSummaryRequestDto) {
        Map<String, Object> param = new HashMap<>();
        /*검증*/
        String cardTrdOnlyYn = StringUtils.isBlank(CommonUtil.nullTrim(tradeUseSummaryRequestDto.getCardTrdOnlyYn())) ? "N" : tradeUseSummaryRequestDto.getCardTrdOnlyYn().toUpperCase();
        if (!"N".equals(cardTrdOnlyYn) && !"Y".equals(cardTrdOnlyYn)) {
            throw new RequestValidationException(ErrorCode.YN_PARAMETER_ERROR, "cardTrdOnlyYn(카드거래 전용 조회 여부)");
        }
        param.put("cardTrdOnlyYn", cardTrdOnlyYn);
        param.put("mpsCustNo", tradeUseSummaryRequestDto.getCustNo());
        param.put("trdDt", tradeUseSummaryRequestDto.getPeriod());

        return tradeRepository.sumTradeUseByMpsCustNoAndTrdDtLike(param);
    }
}

