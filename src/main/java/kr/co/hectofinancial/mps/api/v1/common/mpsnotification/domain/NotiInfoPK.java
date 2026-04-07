package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain;

import javax.persistence.Column;
import java.io.Serializable;
import java.time.LocalDateTime;

public class NotiInfoPK implements Serializable {

    @Column(name = "M_ID", nullable = false)
    private String mid;
    @Column(name = "NOTI_TYPE_CD", nullable = false)
    private String notiTypeCd;
    @Column(name = "ST_DATE", nullable = false)
    private LocalDateTime stDate;
}
