package kr.co.hectofinancial.mps.api.v1.giftcard.single.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardTradeFailPk implements Serializable {

    @Column(name = "TRD_NO", nullable = false)
    private String trdNo;

    @Column(name = "FAIL_DT", nullable = false)
    private String failDt;
}
