package kr.co.hectofinancial.mps.api.v1.market.controller;

import kr.co.hectofinancial.mps.api.v1.common.controller.BaseController;
import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketChargeMapResponseDto;
import kr.co.hectofinancial.mps.api.v1.market.service.MpsMarketChrgMapService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/v1/market")
public class MarketController extends BaseController {
    private final MpsMarketChrgMapService mpsMarketChrgMapService;
    private final CommonService commonService;

    /**
     *
     * @param param
     * @return 포인트 (CP, HP 제외 하고 응답)
     *
     */
    @PostMapping("/charge/list")
    public @ResponseBody ResponseEntity<BaseResponseDto> getMarketChargeList(@RequestBody Map<String,Object> param) {
        Object mid = param.get("mid");
        if (ObjectUtils.isEmpty(mid)) {
            throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
        }
        List<MarketChargeMapResponseDto> chrgMaps = mpsMarketChrgMapService.getMaketChargeMapList(String.valueOf(mid));
        if (chrgMaps.size() == 0) {
            throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
        }
        return ResponseEntity.ok(
                BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(chrgMaps)
                .build()
        );
    }
}
