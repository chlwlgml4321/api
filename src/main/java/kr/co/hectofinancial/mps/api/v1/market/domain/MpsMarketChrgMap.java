package kr.co.hectofinancial.mps.api.v1.market.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_MPS_M_CHRG_MAP",schema = "MPS")
@IdClass(MpsMaketChrgMapPK.class)
public class MpsMarketChrgMap extends BaseEntity {
    @Id
    @Column(name = "M_ID")
    private String mid;
    @Id
    @Column(name = "CHRG_MEAN_CD")
    private String chrgMeanCd;
    @Id
    @Column(name = "ST_DATE")
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime stDate;
    @Column(name = "ED_DATE")
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime edDate;
    @Column(name = "RMK")
    private String rmk;
    @Column(name = "MIN_CHRG_AMT")
    private long minChrgAmt;//최소 충전 금액
    @Column(name = "MIN_CHRG_UNIT_CD")
    private String minChrgUnitCd;//최소 충전 단위 코드
    @Column(name = "DISP_ORD")
    private Integer dispOrd;
    @Column(name = "CHRG_CNCL_PSBL_YN")
    private String chrgCnclPsblYn;
    @Column(name = "WLT_TYPE_DIV_CD")
    private String wltTypeDivCd;
    @Column(name = "CHRG_CNCL_PLC_CD")
    private String chrgCnclPlcCd;

}
