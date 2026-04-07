package kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto;

import kr.co.hectofinancial.mps.global.constant.AutoChargeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 고객 자동충전 정보, 수단(계좌) 등을 가지고 있는 객체
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CustChrgMeanDto {
    private String mpsCustNo;
    private String autoChargeUseYn;//자동충전 사용여부 (자동충전 + 정기충전 전부 아우르는 값)
    private String regularChargeUseYn;//정기충전 사용여부
    private List<CustAutoChargeMethod> autoChargeMethods = Collections.emptyList(); //충전 계좌/카드 정보
    private List<CustAutoChargeInfo> autoChargeInfos = Collections.emptyList(); //자동충전  - 부족분, 기준금액
    private List<CustAutoChargeInfo> regularChargeInfos = Collections.emptyList(); //정기충전 - 요일, 날짜


    /**
     * 자동충전 사용하는지 Yn값
     * @return
     */
    public boolean isAutoChargeUse(){
        return "Y".equalsIgnoreCase(autoChargeUseYn);
    }
    /**
     * 정기충전 사용하는지 Yn값
     * @return
     */
    public boolean isRegularChargeUse(){
        return "Y".equalsIgnoreCase(regularChargeUseYn);
    }

    /**
     * 부족분 자동충전 하는지 확인
     * @return
     */
    public boolean isShortageEnabled() {
        if(autoChargeInfos.isEmpty()) return false;
        return autoChargeInfos.stream().anyMatch(s ->
                s.isUse() && s.getTypeAsEnum() == AutoChargeType.SHORTAGE);
    }

    /**
     * 기준금액 자동충전 하는지 확인
     * @return
     */
    public boolean isThresholdEnabled() {
        if(autoChargeInfos.isEmpty()) return false;
        return autoChargeInfos.stream().anyMatch(s ->
                s.isUse() && s.getTypeAsEnum() == AutoChargeType.THRESHOLD);
    }

    public CustAutoChargeInfo getThresholdInfo() {
        return autoChargeInfos.stream().filter(s -> s.isUse() && s.getTypeAsEnum() == AutoChargeType.THRESHOLD).collect(Collectors.toList()).get(0);
    }
    /**
     * 정기충전 하는지 확인
     *
     * @return
     */
    public boolean isRegularEnabled() {
        if (regularChargeInfos.isEmpty()) return false;
        return regularChargeInfos.stream().anyMatch(s -> s.isUse());
    }
    /**
     * 계좌정보 순서대로
     * @return
     */
    public List<CustAutoChargeMethod> getAutoChargeMethods() {
        List<CustAutoChargeMethod> filterdList = autoChargeMethods.stream()
                .filter(s -> s.getPriority() != null && !s.getPriority().isEmpty()) //priority 값이 없으면 그냥 건너뜀
                .sorted(Comparator.comparingInt(s -> {
                    try {
                        return Integer.parseInt(s.getPriority());
                    } catch (NumberFormatException e) { //exception 발생하면 맨뒤로 보냄
                        log.error("[JSON] 고객={} 자동충전 수단 NumberFormat Exception 발생! {}", mpsCustNo, s.getPriority(), e);
                        return Integer.MAX_VALUE;
                    }
                }))
                .collect(Collectors.toList());
        return filterdList;
    }

}
