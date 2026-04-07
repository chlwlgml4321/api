package kr.co.hectofinancial.mps.api.v1.customer.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class CustChrgMeanPK implements Serializable {

    @Column(name = "CHRG_MEAN_CD", nullable = false)
    private String chrgMeanCd;

    @Column(name = "MPS_CUST_NO", nullable = false)
    private String mpsCustNo;

}
