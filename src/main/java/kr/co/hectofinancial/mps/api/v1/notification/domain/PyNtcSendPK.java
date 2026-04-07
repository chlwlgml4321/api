package kr.co.hectofinancial.mps.api.v1.notification.domain;

import javax.persistence.Column;
import java.io.Serializable;

public class PyNtcSendPK implements Serializable {

    @Column(name = "NTC_SEND_NO")
    private String ntcSendNo;
    @Column(name = "REG_DT")
    private String regDt;

}
