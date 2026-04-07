package kr.co.hectofinancial.mps.api.v1.trade.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class RfdRsltCnfPK implements Serializable {

    @Column(name = "TRD_NO", updatable = false)
    private String trdNo;
    @Column(name = "TRD_DT", updatable = false)
    private String trdDt;
}
