package kr.co.hectofinancial.mps.api.v1.trade.dto.admin;

import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoByMidRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AdminChargeApprovalRequestDto extends CommonInfoByMidRequestDto {
    
    @NotBlank(message = "거래 요청 번호")
    public String trdReqNo;
    @NotBlank(message = "상점 아이디")
    public String mid;
}
