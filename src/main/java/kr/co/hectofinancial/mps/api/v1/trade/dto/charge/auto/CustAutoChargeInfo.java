package kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kr.co.hectofinancial.mps.global.constant.AutoChargeType;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 자동충전정보 (AUTO_CHRG_INFO, RGL_CHRG_INFO) JSON 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({ "linkKey", "useYn", "type", "value", "chgAmt" })
public class CustAutoChargeInfo {
    private String linkKey;
    private String useYn;
    private String type;    // SH: 부족금액충전, ST: 기준금액충전, DW: 정기충전(요일), DT: 정기충전(일)
    private String value;  // SH: 설정 값 없음, ST: 기준금액, DW: 정기충전 요일, DT: 정기충전 일
    private String chgAmt;  // 자동 충전 금액 (SH 제외)

    public boolean isUse(){
        return "Y".equalsIgnoreCase(useYn);
    }

    public AutoChargeType getTypeAsEnum() {
        return AutoChargeType.getAutoChargeType(type);
    }

    public long getTriggerAmountAsLong(){
        if(StringUtils.isBlank(CommonUtil.nullTrim(value))) return 0l;
        return Long.parseLong(value);
    }
    public long getChargeAmountAsLong(){
        if(isUse() && StringUtils.isBlank(CommonUtil.nullTrim(chgAmt))) return 0l;
        return Long.parseLong(chgAmt);
    }
}
