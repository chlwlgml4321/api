package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TB_MPS_BNDL_DSTB", schema = "MPS")
public class GiftCardBundleDistributor {

    @Id
    @Column(name = "GC_DSTB_NO", nullable = false)
    private String gcDstbNo;

    @Column(name = "DSTB_BLC", nullable = false)
    private long dstbBlc;

    @Column(name = "RMK")
    private String rmk;

    @CreatedDate
    @Column(updatable = false, name = "INST_DATE", nullable = false)
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime createdDate;

    @Column(updatable = false, name = "INST_ID", nullable = false)
    @NotNull
    private String createdId;

    @Column(updatable = false, name = "INST_IP", nullable = false)
    @NotNull
    private String createdIp;

    @LastModifiedDate
    @Column(insertable = false, name = "UPDT_DATE")
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime modifiedDate;

    @Column(name = "UPDT_ID")
    private String modifiedId;

    @Column(name = "UPDT_IP")
    private String modifiedIp;
}
