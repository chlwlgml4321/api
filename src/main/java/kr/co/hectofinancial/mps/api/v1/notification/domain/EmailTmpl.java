package kr.co.hectofinancial.mps.api.v1.notification.domain;

import kr.co.hectofinancial.mps.api.v1.firm.domain.DpmnNotiPK;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_EMAIL_TMPL", schema = "TRD")
public class EmailTmpl {

    @Id
    @Column(name = "EMAIL_TMPL_ID")
    private String emailTmplId;
    @Column(name = "EMAIL_TMPL_NM")
    private String emailTmplNm;
    @Column(name = "TITLE")
    private String title;
    @Lob
    @Column(name = "CNTS")
    private String cnts;
    @Column(name = "SEND_EMAIL_ADDR")
    private String sendEmailAddr;
    @Column(name = "USE_YN")
    private String useYn;
    @Column(name = "RMK")
    private String rmk;

    @CreatedDate
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    @Column(updatable = false, name = "INST_DATE")
    private LocalDateTime createdDate;

    @Column(updatable = false, name = "INST_ID")
    private String createdId;

    @Column(updatable = false, name = "INST_IP")
    private String createdIp;

    @LastModifiedDate
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    @Column(insertable = false, name = "UPDT_DATE")
    private LocalDateTime modifiedDate;

    @Column(updatable = true, name = "UPDT_ID")
    private String modifiedId;

    @Column(updatable = true, name = "UPDT_IP")
    private String modifiedIp;

    @Builder
    public EmailTmpl(String emailTmplId, String emailTmplNm, String title, String cnts, String sendEmailAddr, String useYn, String rmk, LocalDateTime createdDate, String createdId, String createdIp, LocalDateTime modifiedDate, String modifiedId, String modifiedIp){

        this.emailTmplId = emailTmplId;
        this.emailTmplNm = emailTmplNm;
        this.title = title;
        this.cnts = cnts;
        this.sendEmailAddr = sendEmailAddr;
        this.useYn = useYn;
        this.rmk = rmk;
        this.createdDate = createdDate;
        this.createdId = createdId;
        this.createdIp = createdIp;
        this.modifiedDate = modifiedDate;
        this.modifiedId = modifiedId;
        this.modifiedIp = modifiedIp;
    }
}
