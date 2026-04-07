package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto;

import kr.co.hectofinancial.mps.api.v1.card.domain.BpcCust;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class NotiCardRequsetDto {

    private String trdNo;
    private String trdDt;
    private String orgTrdNo;
    private String orgTrdDt;
    private String encKey;
    private String pktHashKey;
}
