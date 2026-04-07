package kr.co.hectofinancial.mps.api.v1.common.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_CAL", schema = "BAS")
public class Holiday extends BaseEntity{
    @Id
    @Column(name = "YMD", nullable = false)
    private String ymd;
    @Column(name = "DAY_CD", nullable = false)
    private String dayCd;
    @Column(name = "HLD_STAT_CD", nullable = false)
    private String hldStatCd;
    @Column(name = "RMK", nullable = false)
    private String rmk;
    @Column(name = "LNR_MON_DIV_CD", nullable = false)
    private String lnrMonDivCd;
    @Column(name = "LNR_YMD", nullable = false)
    private String lnrYmd;
}
