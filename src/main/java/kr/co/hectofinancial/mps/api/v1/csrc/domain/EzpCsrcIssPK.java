package kr.co.hectofinancial.mps.api.v1.csrc.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class EzpCsrcIssPK implements Serializable {

    @Column(name = "ISS_REQ_NO", updatable = false)
    private String issReqNo;
    @Column(name = "TRD_DT", updatable = false)
    private String trdDt;
}
