package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "TB_MPS_NOTI_INFO", schema = "MPS")
@IdClass(NotiInfoPK.class)
public class NotiInfo extends BaseEntity {

    @Id
    @Column(name = "NOTI_TYPE_CD")
    private String notiTypeCd;
    @Id
    @Column(name = "ST_DATE")
    private LocalDateTime stDate;
    @Id
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "ED_DATE")
    private LocalDateTime edDate;
    @Column(name = "USE_YN")
    private String useYn;
    @Column(name = "MAX_SEND_CNT")
    private long maxSendCnt;
    @Column(name = "SEND_CYC")
    private long sendCyc;
    @Column(name = "NOTI_URL")
    private String notiUrl;
    @Column(name = "RMK")
    private String rmk;
}
