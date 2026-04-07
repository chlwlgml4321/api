package kr.co.hectofinancial.mps.api.v1.trade.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class TrdDivDtlPK implements Serializable {

    @Column(name = "M_ID", nullable = false)
    private String mid;

    @Column(name = "TRD_DIV_DTL_CD", nullable = false)
    private String trdDivDtlCd;
}
