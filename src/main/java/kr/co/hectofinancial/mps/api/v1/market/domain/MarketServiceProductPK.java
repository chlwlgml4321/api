package kr.co.hectofinancial.mps.api.v1.market.domain;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.Column;
import javax.persistence.Convert;
import java.io.Serializable;
import java.time.LocalDateTime;

public class MarketServiceProductPK implements Serializable {
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "SVC_CD")
    private String svcCd;
    @Column(name = "PRDT_CD")
    private String prdtCd;
}
