package kr.co.hectofinancial.mps.api.v1.common.dto;

import kr.co.hectofinancial.mps.global.annotation.HashField;
import kr.co.hectofinancial.mps.global.annotation.NotLoggableParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 *  요청 dto에 회원번호 없이 mid  만 들어올 경우
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString
public class CommonInfoByCardRequestDto {

    @NotBlank(message = "승인금액")
    private String trdAmt;
    @NotBlank(message = "카드번호")
    private String cardNoEnc;
    @NotBlank(message = "원천사아이디: 비씨/삼성")
    private String storCd; //ornId
    @NotLoggableParam
    public String requestIp;//TMS 로그 내 필요값 (요청 IP)
    @NotLoggableParam
    public long requestStartTime;//TMS 로그 내 필요값 (소요시간 계산용)


}
