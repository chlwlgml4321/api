package kr.co.hectofinancial.mps.api.v1.trade.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class PayMnyPK implements Serializable {

    @Column(name = "LDGR_NO", nullable = false)
    private String ldgrNo;

    @Column(name = "MAKE_DT", nullable = false)
    private String makeDt;

}
