package kr.co.hectofinancial.mps.api.v1.common.dto;

import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.global.annotation.NotLoggableParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 *  조회성 API 요청의 경우 공통으로 포함되는 요청 파라미터
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString
public class CommonInfoRequestDto {


    public String custNo;

    //API 요청값은 아니나, AOP 회원 검증 후 유효한 회원값 담아주는 변수
    @NotLoggableParam
    public CustomerDto customerDto;


    @NotLoggableParam
    public String requestIp;//TMS 로그 내 필요값 (요청 IP)
    @NotLoggableParam
    public long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)


}
