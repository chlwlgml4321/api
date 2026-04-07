package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "TB_MPS_NOTI_SEND", schema = "MPS")
public class NotiSend extends BaseEntity {

    @Id
    @Column(name = "NOTI_SEND_NO")
    private String notiSendNo;
    @Column(name = "SEND_DATE")
    private LocalDateTime sendDate;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "NOTI_TYPE_CD")
    private String notiTypeCd;
    @Column(name = "MAX_SEND_CNT")
    private long maxSendCnt;
    @Column(name = "SEND_CYC")
    private long sendCyc;
    @Column(name = "SEND_RSLT_MSG")
    private String sendRsltMsg;
    @Column(name = "SEND_STAT_CD")
    private String sendStatCd;
    @Column(name = "NOTI_URL")
    private String notiUrl;
    @Column(name = "NOTI_INFO")
    private String notiInfo;
    @Column(name = "NOTI_STOP_YN")
    private String notiStopYn;
    @Column(name = "NOTI_STOP_DATE")
    private LocalDateTime notiStopDate;
    @Column(name = "ACM_SEND_CNT")
    private long acmSendCnt;
    @Column(name = "RMK")
    private String rmk;

    @Builder
    public NotiSend(String notiSendNo, LocalDateTime sendDate, String mid, String notiTypeCd, long maxSendCnt, long sendCyc, String sendRsltMsg,
                    String sendStatCd, String notiUrl, String notiInfo, String notiStopYn, LocalDateTime notiStopDate, long acmSendCnt, String rmk){
        this.notiSendNo = notiSendNo;
        this.sendDate = sendDate;
        this.mid = mid;
        this.notiTypeCd = notiTypeCd;
        this. maxSendCnt = maxSendCnt;
        this.sendCyc = sendCyc;
        this.sendRsltMsg = sendRsltMsg;
        this.sendStatCd = sendStatCd;
        this.notiUrl = notiUrl;
        this.notiInfo = notiInfo;
        this.notiStopYn = notiStopYn;
        this.notiStopDate = notiStopDate;
        this.acmSendCnt = acmSendCnt;
        this.rmk = rmk;
        this.insertCreatedDt();
    }
}
