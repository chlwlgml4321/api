package kr.co.hectofinancial.mps.api.v1.common.dto;

import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import kr.co.hectofinancial.mps.global.annotation.NotLoggableParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * 선불금 사용, 사용취소
 * 충천, 충전취소, 출금
 * API 의 요청 파라미터 내 공통으로 포함되는 값
 * pktHash 에 포함되는 값은 @HashField 필수
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString
public class CommonLogicalRequestDto {

    @HashField(order = 1)
    @NotBlank(message = "회원번호")
    public String custNo;

    @HashField(order = 2)
    @NotBlank(message = "상점거래번호")
    public String mTrdNo;

    @NotBlank(message = "해시데이터")
    public String pktHash;

    //API 요청값은 아니나, AOP 회원 검증 후 유효한 회원값 담아주는 변수
    @NotLoggableParam
    public CustomerDto customerDto;

    @NotLoggableParam
    public String requestIp;//TMS 로그 내 필요값 (요청 IP)
    @NotLoggableParam
    public long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)

}

