package kr.co.hectofinancial.mps.api.v1.trade.domain;

import javax.persistence.Column;
import java.io.Serializable;
import java.time.LocalDateTime;

public class UseMnyPK implements Serializable {

    @Column(name = "LDGR_NO", nullable = false)
    private String ldgrNo;

    @Column(name = "MAKE_DT", nullable = false)
    private String makeDt;

    @Column(name = "USE_DATE", nullable = false)
    private LocalDateTime useDate;
}
