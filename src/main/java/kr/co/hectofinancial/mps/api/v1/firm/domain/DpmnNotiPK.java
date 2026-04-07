package kr.co.hectofinancial.mps.api.v1.firm.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class DpmnNotiPK implements Serializable {

    @Column(name = "GLOBAL_ID")
    private String globalId;
    @Column(name = "TRD_DT")
    private String trdDt;

}
