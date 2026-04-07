package kr.co.hectofinancial.mps.api.v1.trade.controller;

import kr.co.hectofinancial.mps.api.v1.common.controller.BaseController;
import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.notification.service.NotiService;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalCancelResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.ApprovalService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 머니/포인트 충전, 충전취소 Controller
 */
@RestController
@RequestMapping("/v1/approval")
@RequiredArgsConstructor
@Slf4j
public class ApprovalController extends BaseController {

    private final ApprovalService approvalService;
    private final NotiService notiService;

    @PostMapping("/charge")
    public ResponseEntity<BaseResponseDto> approvalCharge( @RequestBody ChargeApprovalRequestDto chargeApprovalRequestDto) {
        ChargeApprovalResponseDto chargeApprovalResponseDto = approvalService.chargeApproval(chargeApprovalRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(chargeApprovalResponseDto).build());
    }

    @PostMapping("/charge/cancel")
    public ResponseEntity<BaseResponseDto> approvalChargeCancel(@RequestBody ChargeApprovalCancelRequestDto chargeApprovalCancelRequestDto) {
        ChargeApprovalCancelResponseDto chargeApprovalCancelResponseDto = approvalService.chargeApprovalCancel(chargeApprovalCancelRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(chargeApprovalCancelResponseDto).build());
    }

}
