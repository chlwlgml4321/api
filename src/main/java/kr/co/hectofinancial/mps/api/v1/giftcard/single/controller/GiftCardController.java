package kr.co.hectofinancial.mps.api.v1.giftcard.single.controller;

import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.*;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.service.GiftCardIssueService;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.service.GiftCardReIssueService;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.service.GiftCardSearchService;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.service.GiftCardUseService;
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
public class GiftCardController {

    private final GiftCardIssueService issueService;
    private final GiftCardUseService useService;
    private final GiftCardSearchService searchService;
    private final GiftCardReIssueService reIssueService;

    /**
     * 상품권 발행
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/issue")
    public ResponseEntity<BaseResponseDto> issue(@RequestBody GiftCardIssueRequestDto dto) {
        GiftCardIssueResponseDto responseDto = issueService.issue(dto);
        return response(responseDto);
    }

    /**
     * 상품권 사용
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/use")
    public ResponseEntity<BaseResponseDto> use(@RequestBody GiftCardUseRequestDto dto) {
        GiftCardUseResponseDto responseDto = useService.useGiftCard(dto);
        return response(responseDto);
    }

    /**
     * 상품권 상태 및 이력 조회
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/search")
    public ResponseEntity<BaseResponseDto> search(@RequestBody GiftCardSearchRequestDto dto) {
        GiftCardSearchResponseDto responseDto = searchService.getGiftCardList(dto);
        return response(responseDto);
    }

    /**
     * 상품권 재발행
     * @param dto
     * @return
     */
    @PostMapping("/v1/giftcard/reissue")
    public ResponseEntity<BaseResponseDto> reIssue(@RequestBody GiftCardReissueRequestDto dto) {
        GiftCardReissueResponseDto responseDto = reIssueService.reIssue(dto);
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
