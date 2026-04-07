package kr.co.hectofinancial.mps.api.v1.trade.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class CustWlltPK implements Serializable {

    @Column(name = "CHRG_MEAN_CD", nullable = false)
    private String chrgMeanCd;

    @Column(name = "BLC_DIV_CD", nullable = false)
    private String blcDivCd;

    @Column(name = "MPS_CUST_NO", nullable = false)
    private String mpsCustNo;
}
