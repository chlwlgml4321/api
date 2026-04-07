package kr.co.hectofinancial.mps.api.v1.market.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_M_SVC_PRDT", schema = "BAS")
@IdClass(MarketServiceProductPK.class)
public class MarketServiceProduct {
    @Id
    @Column(name = "M_ID")
    private String mid;
    @Id
    @Column(name = "SVC_CD")
    private String svcCd;
    @Id
    @Column(name = "PRDT_CD")
    private String prdtCd;
}
