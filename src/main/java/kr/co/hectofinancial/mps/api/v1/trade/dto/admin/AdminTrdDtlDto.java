package kr.co.hectofinancial.mps.api.v1.trade.dto.admin;

import kr.co.hectofinancial.mps.api.v1.trade.domain.AdminTrdDtlPK;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.AdminRsltStatCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AdminTrdDtlDto {

    private String seqNo;
    private String trdReqNo;
    private String mpsCustNo;
    private String rsltStatCd;
    private String trdNo;
    private String trdDt;
    private String trdTm;
    private long trdAmt;
    private Long procBfBlc;
    private Long procAftBlc;
    private String rsltCd;
    private String rsltMsg;
    private String rmk;
    private LocalDateTime createdDate;
    private String createdId;
    private String createdIp;
    private LocalDateTime modifiedDate;
    private String modifiedId;
    private String modifiedIp;

}
