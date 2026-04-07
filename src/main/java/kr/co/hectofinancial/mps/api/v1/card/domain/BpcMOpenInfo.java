package kr.co.hectofinancial.mps.api.v1.card.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "TB_BPC_M_OPEN_INFO", schema = "MPS")
@IdClass(BpcMOpenInfoPK.class)
public class BpcMOpenInfo extends BaseEntity {

    @Id
    @Column(name = "M_ID")
    private String mid;
    @Id
    @Column(name = "ORN_ID")
    private String ornId;
    @Id
    @Column(name = "ST_DATE")
    private Date stDate;
    @Column(name = "ED_DATE")
    private Date edDate;
    @Column(name = "OPEN_STAT_CD")
    private String openStatCd;
    @Column(name = "OPEN_RANGE_CD")
    private String openRangeCd;
    @Column(name = "RMK")
    private String rmk;
}
