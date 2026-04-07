package kr.co.hectofinancial.mps.api.v1.firm.controller;

import kr.co.hectofinancial.mps.api.v1.common.controller.BaseController;
import kr.co.hectofinancial.mps.api.v1.common.dto.FirmResponseDto;
import kr.co.hectofinancial.mps.api.v1.firm.dto.FirmDepositeNoticeRequestDto;
import kr.co.hectofinancial.mps.api.v1.firm.service.DpmnNotiService;
import kr.co.hectofinancial.mps.api.v1.firm.service.FirmBankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

@Controller
@RequestMapping("/v1/firm")
@RequiredArgsConstructor
public class FirmBankingController extends BaseController {

    private final FirmBankingService firmBankingService;
    private final DpmnNotiService dpmnNotiService;

    @PostMapping("/deposite/notice")
    @ResponseBody
    public ResponseEntity<FirmResponseDto> depositeNotice(@Valid @RequestBody FirmDepositeNoticeRequestDto firmDepositeNoticeRequestDto) throws Exception {

        return ResponseEntity.ok(FirmResponseDto.builder()
                .data(dpmnNotiService.depositeNotice(firmDepositeNoticeRequestDto)).build());
    }

}
