package kr.co.hectofinancial.mps.api.v1.market.dto;

import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarketChrgMap;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class MarketChargeMapResponseDto {
    private String mid;
    private String chrgMeanCd;
    private String chrgMeanNm;
    private String stDate;
    private String edDate;
    private String rmk;
    private long minChrgAmt;
    private String minChrgUnitCd;
    private Integer order;
    private String chrgCnclPsblYn;


    public static MarketChargeMapResponseDto of(MpsMarketChrgMap mpsMarketChrgMap) {
        return MarketChargeMapResponseDto.builder()
                .mid(mpsMarketChrgMap.getMid())
                .chrgMeanCd(mpsMarketChrgMap.getChrgMeanCd())
                .stDate(DateTimeUtil.convertStringDateTime(mpsMarketChrgMap.getStDate()))
                .edDate(DateTimeUtil.convertStringDateTime(mpsMarketChrgMap.getEdDate()))
                .rmk(CommonUtil.nullTrim(mpsMarketChrgMap.getRmk()))
                .minChrgAmt(mpsMarketChrgMap.getMinChrgAmt())
                .minChrgUnitCd(mpsMarketChrgMap.getMinChrgUnitCd())
                .order(mpsMarketChrgMap.getDispOrd() == null ? 99 : mpsMarketChrgMap.getDispOrd())
                .chrgCnclPsblYn(mpsMarketChrgMap.getChrgCnclPsblYn())
                .build();
    }

    public static MarketChargeMapResponseDto of(Map<String,Object> obj) {
        return MarketChargeMapResponseDto.builder()
                .mid((String) obj.get("M_ID"))
                .chrgMeanCd((String) obj.get("CHRG_MEAN_CD"))
                .chrgMeanNm((String) obj.get("CHRG_MEAN_NM"))
                .stDate(DateTimeUtil.convertStringDateTime(((Timestamp) obj.get("ST_DATE")).toLocalDateTime()))
                .edDate(DateTimeUtil.convertStringDateTime(((Timestamp) obj.get("ED_DATE")).toLocalDateTime()))
                .chrgCnclPsblYn((String) obj.get("CHRG_CNCL_PSBL_YN"))
                .rmk(ObjectUtils.isEmpty(obj.get("RMK")) ? "" : (String) obj.get("RMK"))
                .minChrgAmt(ObjectUtils.isEmpty(obj.get("MIN_CHRG_AMT")) ? null : ((BigDecimal) obj.get("MIN_CHRG_AMT")).longValue())
                .minChrgUnitCd(ObjectUtils.isEmpty(obj.get("MIN_CHRG_UNIT_CD")) ? "" : (String) obj.get("MIN_CHRG_UNIT_CD"))
                .order(ObjectUtils.isEmpty(obj.get("DISP_ORD")) ? 99 : ((BigDecimal) obj.get("DISP_ORD")).intValue())//todo 상의 필요
                .build();
    }

}
