package kr.co.hectofinancial.mps.api.v1.trade.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_MPS_CUST_WLLT", schema = "MPS")
@IdClass(CustWlltPK.class)
public class CustWllt extends BaseEntity {

    @Id
    @Column(name = "CHRG_MEAN_CD", nullable = false)
    private String chrgMeanCd;

    @Id
    @Column(name = "BLC_DIV_CD", nullable = false)
    private String blcDivCd;

    @Id
    @Column(name = "MPS_CUST_NO", nullable = false)
    private String mpsCustNo;

    @Column(name = "LAST_LDGR_NO", nullable = false)
    private String lastLdgrNo;

    @Column(name = "LAST_MAKE_DT", nullable = false)
    private String lastMakeDt;

    @Column(name = "BLC", nullable = false)
    private long blc;

}
