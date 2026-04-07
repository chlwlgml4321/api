package kr.co.hectofinancial.mps.api.v1.deposit.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class DpmnRcvDtlPK implements Serializable {

    @Column(name = "TRD_DT")
    private String trdDt;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "DP_NOTI_NO")
    private String dpNotiNo;

}
