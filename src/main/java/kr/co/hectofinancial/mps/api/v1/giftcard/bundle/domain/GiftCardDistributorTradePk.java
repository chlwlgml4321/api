package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardDistributorTradePk implements Serializable {

    @Column(name = "DSTB_TRD_NO", nullable = false)
    private String dstbTrdNo;

    @Column(name = "TRD_DT", nullable = false)
    private String trdDt;
}