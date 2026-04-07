package kr.co.hectofinancial.mps.api.v1.giftcard.single.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardIssuePk implements Serializable {

    @Column(name = "GC_NO_ENC", nullable = false)
    private String gcNoEnc;

    @Column(name = "ISS_DT", nullable = false)
    private String issDt;
}
