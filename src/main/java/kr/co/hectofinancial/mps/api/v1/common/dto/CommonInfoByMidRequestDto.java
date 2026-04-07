package kr.co.hectofinancial.mps.api.v1.common.dto;

import kr.co.hectofinancial.mps.global.annotation.NotLoggableParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 *  요청 dto에 회원번호 없이 mid  만 들어올 경우
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString
public class CommonInfoByMidRequestDto {


    public String mid;
    @NotLoggableParam
    public String requestIp;//TMS 로그 내 필요값 (요청 IP)
    @NotLoggableParam
    public long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)


}
