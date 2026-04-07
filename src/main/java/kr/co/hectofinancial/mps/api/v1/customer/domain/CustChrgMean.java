package kr.co.hectofinancial.mps.api.v1.customer.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_MPS_CUST_CHRG_MEAN", schema = "MPS")
@IdClass(CustChrgMeanPK.class)
public class CustChrgMean extends BaseEntity {

    @Id
    @Column(name = "MPS_CUST_NO", nullable = false)
    private String mpsCustNo;
    @Id
    @Column(name = "CHRG_MEAN_CD", nullable = false)
    private String chrgMeanCd;

    @Column(name = "CHRG_MEAN_DTL")
    private String chrgMeanDtl;

    @Column(name = "AUTO_CHRG_YN")
    private String autoChrgYn; //자동충전 여부
    @Column(name = "RGL_CHRG_USE_YN")
    private String rglChrgUseYn; //정기충전 여부
    @Column(name = "AUTO_CHRG_ACNT")
    private String autoChrgAcnt; //자동충전 계좌 (주계좌 , 보조계좌(옵션))
    @Column(name = "AUTO_CHRG_INFO")
    private String autoChrgInfo; //자동충전 정보
    @Column(name = "RGL_CHRG_INFO")
    private String rglChrgInfo; //정기충전 정보

}
