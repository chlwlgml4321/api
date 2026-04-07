package kr.co.hectofinancial.mps.api.v1.market.domain;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.Column;
import javax.persistence.Convert;
import java.io.Serializable;
import java.time.LocalDateTime;

public class MpsMaketChrgMapPK implements Serializable {
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "CHRG_MEAN_CD")
    private String chrgMeanCd;
    @Column(name = "ST_DATE")
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime stDate;
}
