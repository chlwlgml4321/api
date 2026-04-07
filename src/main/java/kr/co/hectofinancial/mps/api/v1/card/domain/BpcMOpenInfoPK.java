package kr.co.hectofinancial.mps.api.v1.card.domain;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

public class BpcMOpenInfoPK implements Serializable {

    @Column(name = "M_ID")
    private String mid;
    @Column(name = "ORN_ID")
    private String ornId;
    @Column(name = "ST_DATE")
    private Date stDate;
}
