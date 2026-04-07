package kr.co.hectofinancial.mps.api.v1.trade.controller;

import kr.co.hectofinancial.mps.api.v1.common.controller.BaseController;
import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminChargeApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.ApprovalService;
import kr.co.hectofinancial.mps.api.v1.trade.service.admin.AsyncService;
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
@Slf4j
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController extends BaseController {

    private final ApprovalService approvalService;
    private final AsyncService asyncService;

    @PostMapping("/manual/trade")
    public ResponseEntity<BaseResponseDto> adminCharge(@RequestBody AdminChargeApprovalRequestDto adminChargeApprovalRequestDto) throws Exception {
        AdminChargeApprovalResponseDto adminChargeApprovalResponseDto = approvalService.chkAdminCharge(adminChargeApprovalRequestDto);
        log.info(">>>> CONTROLLER 관리자 수기 머니/포인트 async-start <<<<");
        asyncService.adminTrade(adminChargeApprovalRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(adminChargeApprovalResponseDto)
                .build());
    }
}
