package kr.co.hectofinancial.mps.api.v1.market.domain;

import javax.persistence.Column;
import java.io.Serializable;
import java.time.LocalDateTime;

public class MarketAddInfoPK implements Serializable {

    @Column(name = "M_ID", updatable = false)
    private String mid;
    @Column(name = "ST_DATE", updatable = false)
    private LocalDateTime stDate;
}
