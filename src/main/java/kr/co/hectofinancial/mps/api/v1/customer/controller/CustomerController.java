package kr.co.hectofinancial.mps.api.v1.customer.controller;

import kr.co.hectofinancial.mps.api.v1.common.controller.BaseController;
import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerRequestDto;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerResponseDto;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 회원 관련 Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/customer")
public class CustomerController extends BaseController {
    private final CommonService commonService;

    /**
     * 회원정보조회
     * @param customerRequestDto
     * @return customerInfoResponseDto
     * @author: hyeyoungji
     */
    @PostMapping("/info")
    public ResponseEntity<BaseResponseDto> getCustomerInfo(@RequestBody CustomerRequestDto customerRequestDto) throws Exception {

        CustomerDto customerDto = commonService.getValidCustomerForCustomerInfo(customerRequestDto);

        return ResponseEntity.ok(BaseResponseDto.builder()
                        .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                        .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                        .rsltObj(CustomerResponseDto.of(customerDto))
                        .build());
    }
}
