package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundleRequestPk implements Serializable {

    @Column(name = "REQ_NO", nullable = false)
    private String reqNo;

    @Column(name = "REQ_DT", nullable = false)
    private String reqDt;
}
