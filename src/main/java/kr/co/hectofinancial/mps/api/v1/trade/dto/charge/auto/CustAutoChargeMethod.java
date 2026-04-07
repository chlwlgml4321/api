package kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kr.co.hectofinancial.mps.global.constant.AutoChargeMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 자동충전수단 (AUTO_CHRG_ACCNT) JSON 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"linkKey", "pmtType", "pmtCode", "pmtName", "pmtNoSuffix", "pmtKey", "priority"})
public class CustAutoChargeMethod {

    private String linkKey; // 결제 수단 식별키
    private String pmtType; //A:계좌 , C:카드
    private String pmtCode;
    private String pmtName;
    private String pmtNoSuffix;
    private String pmtKey;
    private String priority;  // 0번이 주, 0이 아닌 경우 보조 & 우선순위

    public AutoChargeMethodType getTypeAsEnum() {
        return AutoChargeMethodType.getAutoChargeAccountType(pmtType);
    }
}
