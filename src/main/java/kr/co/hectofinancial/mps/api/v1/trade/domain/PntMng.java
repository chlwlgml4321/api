package kr.co.hectofinancial.mps.api.v1.trade.domain;

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
@Table(name = "TB_MPS_PNT_MNG", schema = "MPS")
public class PntMng extends BaseEntity {

    @Id
    @Column(name = "PNT_ID")
    private String pntId;
    @Column(name = "PNT_NM")
    private String pntNm;
    @Column(name = "PNT_VLD_DT_CNT")
    private String pntVldDtCnt;
    @Column(name = "PAY_HOST_CD")
    private String payHostCd;
    @Column(name = "RMK")
    private String rmk;
}
