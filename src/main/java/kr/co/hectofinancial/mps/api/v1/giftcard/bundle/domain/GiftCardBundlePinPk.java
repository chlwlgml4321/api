package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundlePinPk implements Serializable {

    @Column(name = "BNDL_PIN_NO_ENC", nullable = false)
    private String bndlPinNoEnc;

    @Column(name = "ISS_DT", nullable = false)
    private String issDt;
}
