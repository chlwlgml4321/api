package kr.co.hectofinancial.mps.api.v1.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class PyNtcSendInsertRequestDto {

    private String mpsCustNo;
    private String custNm;
    private String email;
    private String trdNo;
    private String amt;
    private String mnyAmt;
    private String pntAmt;
    private String chrgMeanCd;
    private String trdDtm;
    private String storNm;
    private String stlMid;
    private String mid;
    private String msgTmplId;

}
