package kr.co.hectofinancial.mps.api.v1.cpn.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_CPN_M", schema = "BAS")
@IdClass(CpnMPK.class)
public class CpnM extends BaseEntity {

    @Id
    @Column(name = "M_ID", insertable = false, updatable = false)
    private String mid;

    @Id
    @Column(name = "ST_DT", insertable = false, updatable = false)
    private String stDt;

    @Column(name = "ED_DT", insertable = false, updatable = false)
    private String edDt;

    @Column(name = "CPN_ID", insertable = false, updatable = false)
    private String cpnId;

    @Column(name = "CHG_RSN_CD", insertable = false, updatable = false)
    private String chgRsnCd;

    @Column(name = "RMK", insertable = false, updatable = false)
    private String rmk;

}
