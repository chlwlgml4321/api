package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.controller;

import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.GiftCardBundleChargeEtcRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.GiftCardBundleChargeEtcResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.cancel.GiftCardBundleChargeCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.cancel.GiftCardBundleChargeCancelResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue.GiftCardBundleIssueRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue.GiftCardBundleIssueResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.*;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.transfer.GiftCardBundleTransferRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.transfer.GiftCardBundleTransferResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.GiftCardBundleUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.GiftCardBundleUseResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.GiftCardBundleBalanceUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.GiftCardBundleBalanceUseResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.cancel.GiftCardBundleBalanceUseCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.cancel.GiftCardBundleBalanceUseCancelResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.charge.GiftCardBundleChargeEtcService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.charge.cancel.GiftCardBundleChargeCancelService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.issue.GiftCardBundleIssueService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.search.GiftCardBundleBalanceService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.search.GiftCardBundleInfoService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.search.GiftCardBundleListService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.transfer.GiftCardBundleTransferService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.use.GiftCardBundleUseService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GiftCardBundleController {

    private final GiftCardBundleChargeCancelService chargeCancelService;
    private final GiftCardBundleChargeEtcService chargeEtcService;
    private final GiftCardBundleIssueService issueService;
    private final GiftCardBundleTransferService transferService;
    private final GiftCardBundleUseService useService;
    private final GiftCardBundleBalanceService balanceService;
    private final GiftCardBundleInfoService infoService;
    private final GiftCardBundleListService listService;

    /**
     * 유통잔액충전
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/bundle/charge/etc")
    public ResponseEntity<BaseResponseDto> chargeEtc(@RequestBody GiftCardBundleChargeEtcRequestDto dto) {
        GiftCardBundleChargeEtcResponseDto responseDto = chargeEtcService.charge(dto);
        return response(responseDto);
    }

    /**
     * 묶음상품권 발행
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/bundle/issue")
    public ResponseEntity<BaseResponseDto> issue(@RequestBody GiftCardBundleIssueRequestDto dto) {
        GiftCardBundleIssueResponseDto responseDto = issueService.issue(dto);
        return response(responseDto);
    }

    /**
     * 묶음상품권 양도
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/bundle/transfer")
    public ResponseEntity<BaseResponseDto> transfer(@RequestBody GiftCardBundleTransferRequestDto dto) {
        GiftCardBundleTransferResponseDto responseDto = transferService.transfer(dto);
        return response(responseDto);
    }

    /**
     * 묶음상품권 사용
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/bundle/use")
    public ResponseEntity<BaseResponseDto> use(@RequestBody GiftCardBundleUseRequestDto dto) {
        GiftCardBundleUseResponseDto responseDto = useService.use(dto);
        return response(responseDto);
    }

    /**
     * 묶음상품권 상세 조회
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/bundle/info")
    public ResponseEntity<BaseResponseDto> getInfo(@RequestBody GiftCardBundleInfoRequestDto dto) {
        GiftCardBundleInfoResponseDto responseDto = infoService.getInfo(dto);
        return response(responseDto);
    }

    /**
     * 묶음상품권 잔액 조회
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/bundle/balance")
    public ResponseEntity<BaseResponseDto> getBalance(@RequestBody GiftCardBundleBalanceRequestDto dto) {
        GiftCardBundleBalanceResponseDto responseDto = balanceService.getBalance(dto);
        return response(responseDto);
    }

    /**
     * 묶음상품권 목록 조회
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/bundle/list")
    public ResponseEntity<BaseResponseDto> getRecentList(@RequestBody GiftCardBundleListRequestDto dto) {
        GiftCardBundleListResponseDto responseDto = listService.getBundleList(dto);
        return response(responseDto);
    }

    /**
     * 유통 잔액 충전 취소
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/bundle/charge/cancel")
    public ResponseEntity<BaseResponseDto> chargeCancel(@RequestBody GiftCardBundleChargeCancelRequestDto dto) {
        GiftCardBundleChargeCancelResponseDto responseDto = chargeCancelService.chargeCancel(dto);
        return response(responseDto);
    }

    /**
     * 유통 잔액 사용 - 선불 PIN 충전
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/bundle/balance/use")
    public ResponseEntity<BaseResponseDto> useBalance(@RequestBody GiftCardBundleBalanceUseRequestDto dto){
    	GiftCardBundleBalanceUseResponseDto responseDto = balanceService.use(dto);
        return response(responseDto);
    }

    /**
     * 유통 잔액 사용 취소
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/bundle/balance/use/cancel")
    public ResponseEntity<BaseResponseDto> useCancelBalance(@RequestBody GiftCardBundleBalanceUseCancelRequestDto dto){
        GiftCardBundleBalanceUseCancelResponseDto responseDto = balanceService.useCancel(dto);
        return response(responseDto);
    }

    private ResponseEntity<BaseResponseDto> response(Object responseDto) {
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(responseDto)
                .build());
    }
}
