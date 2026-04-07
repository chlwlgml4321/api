package kr.co.hectofinancial.mps.api.v1.trade.dto.money;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RfdRcptInsertDto {

    private String rfdRcptNo;
    private String mid;
    private String svcCd;
    private String prdtCd;
    private String rcptDt;
    private String rcptTm;
    private long orgTrdAmt;
    private long rfdAmt;
    private String rfdDt; //실제 환불일자
    private String rfdSchDt;
    private String rfdAcntBankCd;
    private String rfdAcntNoMsk;
    private String rfdAcntNoEnc;
    private String rfdAcntDprNm;
    private String rfdAcntSumry;
    private String rfdStatCd;
    private String macntBankCd;
    private String macntNoEnc;
    private String macntSumry;
    private String macntNoMsk;
    private long retryCnt;
    private String rfdApprStatCd;
    private String rmk;
    private String trsfMid;
}
