package kr.co.hectofinancial.mps.api.v1.deposit.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class DstbDpmnRcvPK implements Serializable {

    @Column(name = "TRD_DT")
    private String trdDt;
    @Column(name = "M_ID")
    private String mid;

}
