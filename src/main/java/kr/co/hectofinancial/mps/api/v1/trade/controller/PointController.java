package kr.co.hectofinancial.mps.api.v1.trade.controller;

import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.GetExpPntByCustNoRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.GetExpPntByCustNoResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.PointRevokeRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.PointRevokeResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.service.PointService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1")
public class PointController {

    private final PointService pointService;

    @PostMapping("/get/customers/point/expiring")
    public ResponseEntity<BaseResponseDto> getExpPntByCustNo(@RequestBody GetExpPntByCustNoRequestDto getExpPntByCustNoRequestDto) {
        GetExpPntByCustNoResponseDto getExpPntByCustNoResponseDto = pointService.getExpPntByCustNo(getExpPntByCustNoRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(getExpPntByCustNoResponseDto)
                .build());
    }

    @PostMapping("/point/revoke")
    public ResponseEntity<BaseResponseDto> pointRevoke(@RequestBody PointRevokeRequestDto pointRevokeRequestDto){
        PointRevokeResponseDto pointRevokeResponseDto = pointService.pointRevoke(pointRevokeRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(pointRevokeResponseDto)
                .build());
    }

    /* 2025-05-26 소멸예정 포인트 조회(가맹점별) 배치로 전환 */
//    @PostMapping("/get/mid/point/expiring")
//    public ResponseEntity<BaseResponseDto> getExpPntByMid(@RequestBody GetExpPntByMidRequestDto getExpPntByMidRequestDto) {
//        GetExpPntByMidResponseDto getExpPntByCustNoRequestDto = pointService.getExpPntByMid(getExpPntByMidRequestDto);
//        return ResponseEntity.ok(BaseResponseDto.builder()
//                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
//                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
//                .rsltObj(getExpPntByCustNoRequestDto)
//                .build());
//    }
}
