package kr.co.hectofinancial.mps.api.v1.trade.controller;

import kr.co.hectofinancial.mps.api.v1.common.controller.BaseController;
import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminWithdrawApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminWithdrawApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.*;
import kr.co.hectofinancial.mps.api.v1.trade.service.money.MoneyService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/money")
@RequiredArgsConstructor
public class MoneyController extends BaseController {

    private final MoneyService moneyService;

    @PostMapping("/withdrawal")
    public ResponseEntity<BaseResponseDto> moneyWithdraw(@RequestBody WithdrawApprovalRequestDto withdrawApprovalRequestDto) throws Exception {
        WithdrawApprovalResponseDto withdrawApprovalResponseDto = moneyService.moneyWithdraw(withdrawApprovalRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(withdrawApprovalResponseDto)
                .build());
    }

    @PostMapping("/wait/withdrawal")
    public ResponseEntity<BaseResponseDto> wailMoneyWithdraw(@RequestBody WaitMnyWithdrawApprovalRequestDto waitMnyWithdrawApprovalRequestDto) throws Exception {
        WaitMnyWithdrawApprovalResponseDto waitMnyWithdrawApprovalResponseDto = moneyService.waitMoneyWithdraw(waitMnyWithdrawApprovalRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(waitMnyWithdrawApprovalResponseDto)
                .build());
    }

    @PostMapping("/gift")
    public ResponseEntity<BaseResponseDto> moneyGift(@RequestBody MoneyGiftRequestDto moneyGiftRequestDto) throws Exception {
        MoneyGiftResponseDto moneyGiftResponseDto = moneyService.moneyGift(moneyGiftRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(moneyGiftResponseDto)
                .build());
    }

    @PostMapping("/willMnyWdYn")
    public ResponseEntity<BaseResponseDto> willMnyWithdrawalYn(@Valid @RequestBody WillMnyWithdrawalYnRequestDto willMnyWithdrawalYnRequestDto) throws Exception {
        WillMnyWithdrawalYnResponseDto willMnyWithdrawalYnResponseDto = moneyService.willMnyWithdrawalYn(willMnyWithdrawalYnRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(willMnyWithdrawalYnResponseDto)
                .build());
    }

    @PostMapping("/withdrawal/retry")
    public ResponseEntity<BaseResponseDto> moneyWithdrawRetry(@RequestBody RetryMoneyWithdrawRequestDto retryMoneyWithdrawRequestDto) throws Exception {
        RetryMoneyWithdrawResponseDto retryMoneyWithdrawResponseDto = moneyService.retryMoneyWithdraw(retryMoneyWithdrawRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(retryMoneyWithdrawResponseDto)
                .build());
    }

    @PostMapping("/admin/withdrawal")
    public ResponseEntity<BaseResponseDto> adminWithdraw(@RequestBody AdminWithdrawApprovalRequestDto adminWithdrawApprovalRequestDto) {
        AdminWithdrawApprovalResponseDto adminWithdrawApprovalResponseDto = moneyService.adminWithdraw(adminWithdrawApprovalRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(adminWithdrawApprovalResponseDto)
                .build());
    }
}
