package kr.co.hectofinancial.mps.api.v1.trade.dto.point;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class GetExpPntByCustNoResponseDto {
    private String custNo;
    private String expPntBlc;//3D 이내 소멸예정 포인트
}
