package kr.co.hectofinancial.mps.api.v1.trade.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_MPS_M_TRD_DIV_DTL", schema = "MPS")
@IdClass(TrdDivDtlPK.class)
public class TrdDivDtl extends BaseEntity {

    @Id
    @Column(name = "M_ID")
    private String mid;

    @Id
    @Column(name = "TRD_DIV_DTL_CD")
    private String trdDivDtlCd;

    @Column(name = "TRD_DIV_DTL_NM")
    private String trdDivDtlNm;

    @Column(name = "TRD_DIV_CD")
    private String trdDivCd;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "RMK")
    private String rmk;

    @Column(name = "PNT_BILL_M_ID")
    private String pntBillMid;

}
