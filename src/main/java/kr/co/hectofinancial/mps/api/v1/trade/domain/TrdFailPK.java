package kr.co.hectofinancial.mps.api.v1.trade.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class TrdFailPK implements Serializable {

    @Column(name = "TRD_NO")
    private String trdNo;
    @Column(name = "FAIL_DT")
    private String failDt;
}
