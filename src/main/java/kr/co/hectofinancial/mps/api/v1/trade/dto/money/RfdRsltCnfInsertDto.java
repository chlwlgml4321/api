package kr.co.hectofinancial.mps.api.v1.trade.dto.money;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RfdRsltCnfInsertDto {

    private String trdNo;
    private String trdDt;
    private String trdTm;
    private String rfdTrdNo;
    private String mid;
    private String mpsCustNo;
    private long trdAmt;
    private String rsltCnfStatCd;
    private LocalDateTime lastRsltCnfDate;
    private long rsltCnfCnt;
    private String reprocRfdTrdNo;
    private LocalDateTime reprocRfdDate;
    private String reprocRfdStatCd;
    private String reprocRfdRsltCd;
    private String rmk;
    private String rfdAcntBankCd;
    private String rfdAcntNoMsk;
    private String rfdAcntNoEnc;
    private String rmtDivCd;
}
