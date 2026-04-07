package kr.co.hectofinancial.mps.api.v1.firm.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class FcrcSumryPK implements Serializable {

    @Column(name = "ACNT_SUMRY")
    private String acntSumry;
    @Column(name = "VLD_ED_DATE")
    private String vldEdDate;

}
