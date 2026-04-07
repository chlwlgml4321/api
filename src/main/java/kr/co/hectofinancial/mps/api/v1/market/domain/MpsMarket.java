package kr.co.hectofinancial.mps.api.v1.market.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_MPS_M", schema = "MPS")
public class MpsMarket extends BaseEntity {
    @Id
    @Column(name = "M_ID", updatable = false)
    private String mid;
    @Column(name = "USE_YN", updatable = false)
    private String useYn;
    @Column(name = "RMK", updatable = false)
    private String rmk;
    @Column(name = "CUST_JOIN_TYPE_CD", updatable = false)
    private String custJoinTypeCd;
    @Column(name = "MON_WD_LMT_CNT", updatable = false)
    private long monWdLmtCnt;
    @Column(name = "WD_TYPE_CD", updatable = false)
    private String wdTypeCd;
    @Column(name = "BILL_KEY_USE_YN", updatable = false)
    private String billKeyUseYn;
    @Column(name = "CUST_BIZ_DIV_CD", updatable = false)
    private String custBizDivCd;
    @Column(name = "PIN_VRIFY_YN", updatable = false)
    private String pinVrifyYn;
    @Column(name = "PIN_VRIFY_TYPE_CD", updatable = false)
    private String pinVrifyTypeCd;
    @Column(name = "WD_TRD_SUMRY", updatable = false)
    private String wdTrdSumry;
    @Column(name = "AUTO_CHRG_MEAN_CD", updatable = false)
    private String autoChrgMeanCd;//Y:All, N:미사용, A:account(계좌), C:카드
}
