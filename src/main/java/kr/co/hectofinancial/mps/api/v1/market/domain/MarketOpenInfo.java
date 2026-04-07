package kr.co.hectofinancial.mps.api.v1.market.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_M_OPEN_INFO", schema = "BAS")
public class MarketOpenInfo extends BaseEntity{

    @Id
    @Column(name = "ST_DATE")
    private String stDate;
    @Column(name = "M_ID")
    private String mid;

    @Column(name = "SVC_CD")
    private String svcCd;

    @Column(name = "PRDT_CD")
    private String prdtCd;
    @Column(name = "ED_DATE")
    private LocalDateTime edDate;
    @Column(name = "OPEN_STAT_CD")
    private String openStatCd;
    @Column(name = "OPEN_RANGE_CD")
    private String openRangeCd;
    @Column(name = "CHG_RSN_CD")
    private String chgRsnCd;
    @Column(name = "RMK")
    private String rmk;

}
