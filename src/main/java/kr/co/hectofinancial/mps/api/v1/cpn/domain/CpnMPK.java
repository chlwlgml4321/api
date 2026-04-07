package kr.co.hectofinancial.mps.api.v1.cpn.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class CpnMPK implements Serializable {

    @Id
    @Column(name = "M_ID", insertable = false, updatable = false)
    private String mid;
    @Id
    @Column(name = "ST_DT", insertable = false, updatable = false)
    private String stDt;

}
