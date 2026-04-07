package kr.co.hectofinancial.mps.api.v1.trade.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class AdminTrdDtlPK implements Serializable {

    @Column(name = "SEQ_NO", nullable = false)
    private String seqNo;

    @Column(name = "TRD_REQ_NO", nullable = false)
    private String trdReqNo;
}
