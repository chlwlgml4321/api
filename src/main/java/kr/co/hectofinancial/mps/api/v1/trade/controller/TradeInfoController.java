package kr.co.hectofinancial.mps.api.v1.trade.controller;

import kr.co.hectofinancial.mps.api.v1.common.controller.BaseController;
import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.authentication.dto.GetBillKeyRequestDto;
import kr.co.hectofinancial.mps.api.v1.authentication.dto.GetBillKeyResponseDto;
import kr.co.hectofinancial.mps.api.v1.authentication.service.BillKeyService;
import kr.co.hectofinancial.mps.api.v1.trade.dto.*;
import kr.co.hectofinancial.mps.api.v1.trade.service.TradeInfoService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 거래 내역 Controller
 */
@RestController
@RequestMapping("/v1/trade")
@RequiredArgsConstructor
public class TradeInfoController extends BaseController {

    private final TradeInfoService tradeInfoService;
    private final BillKeyService billKeyService;

    @PostMapping("/list")
    public ResponseEntity<BaseResponseDto> getTradeList(@RequestBody TradeInfoListRequestDto tradeInfoListRequestDto) throws Exception {
        List<TradeInfoListResponseDto> trades = tradeInfoService.getTradesByCustNoAndConditions(tradeInfoListRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(trades).build());
    }

    @PostMapping("/detail")
    public ResponseEntity<BaseResponseDto> getTradeInfo( @RequestBody TradeInfoRequestDto tradeInfoRequestDto) throws Exception {
        TradeInfoResponseDto tradeByTrdNoAndTrdDt = tradeInfoService.getTradeByTrdNoAndTrdDt(tradeInfoRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(tradeByTrdNoAndTrdDt).build());
    }
    @PostMapping("/detail/market")
    public ResponseEntity<BaseResponseDto> getTradeInfoByMarketTradeNo(@RequestBody TradeInfoByMarketRequestDto tradeInfoByMarketRequestDto) throws Exception {
        List<TradeInfoResponseDto> trades = tradeInfoService.findTradesByMpsCustNoAndPeriodAndMTrdNo(tradeInfoByMarketRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(trades).build());
    }

    @PostMapping("/getBillKey")
    public ResponseEntity<BaseResponseDto> getBillKey(@RequestBody GetBillKeyRequestDto getBillKeyRequestDto) {
        GetBillKeyResponseDto getBillKeyResponseDto = billKeyService.getBillKey(getBillKeyRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(getBillKeyResponseDto).build());
    }

    @PostMapping("/use/summary")
    public ResponseEntity<BaseResponseDto> getTradeUseSum(@RequestBody TradeUseSummaryRequestDto tradeUseSummaryRequestDto) {
        TradeUseSummaryResponseDto trd = tradeInfoService.getTradeUseSum(tradeUseSummaryRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(trd).build());
    }
}
