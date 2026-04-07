package kr.co.hectofinancial.mps.api.v1.notification.domain;

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
@Table(name = "TB_SITE_POLICY_MAST", schema = "OPM")
public class SitePolicyMast {

    @Id
    @Column(name = "META_KEY")
    private String metaKey;
    @Column(name = "META_VALUE")
    private String metaValue;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "REALTIME_APPLY_YN")
    private String realTimeApplyYn;
    @Column(name = "REG_ACCNT_SEQ")
    private long regAccntSeq;
    @CreatedDate
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    @Column(name = "REG_DT")
    private LocalDateTime regDt;
    @LastModifiedDate
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    @Column(updatable = true, name = "UPDATE_DT")
    private LocalDateTime updateDt;

    @Column(name = "UPDATE_ACCNT_SEQ")
    private long updateAccntSeq;

    @Builder
    public SitePolicyMast(String metaKey, String metaValue, String description, String realTimeApplyYn, long regAccntSeq, long updateAccntSeq, LocalDateTime regDt, LocalDateTime updateDt){
        this.metaKey = metaKey;
        this.metaValue = metaValue;
        this.description = description;
        this.realTimeApplyYn = realTimeApplyYn;
        this.regAccntSeq = regAccntSeq;
        this.updateAccntSeq = updateAccntSeq;
        this.regDt = regDt;
        this.updateDt = updateDt;
    }
}
