package kr.co.hectofinancial.mps.api.v1.csrc.controller;

import kr.co.hectofinancial.mps.api.v1.common.controller.BaseController;
import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.csrc.dto.CashRcptRegistResponseDto;
import kr.co.hectofinancial.mps.api.v1.csrc.dto.CashRcptResistRequestDto;
import kr.co.hectofinancial.mps.api.v1.csrc.service.CashRcptService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/csrc")
@RequiredArgsConstructor
@Slf4j
public class CashRcptController extends BaseController {

    private final CashRcptService cashRcptService;

    @PostMapping("/resist")
    public ResponseEntity<BaseResponseDto> cashRcptResist(@RequestBody CashRcptResistRequestDto cashRcptResistRequestDto) throws Exception {
        CashRcptRegistResponseDto cashRcptRegistResponseDto = cashRcptService.cashRcptResist(cashRcptResistRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(cashRcptRegistResponseDto)
                .build());
    }

}
