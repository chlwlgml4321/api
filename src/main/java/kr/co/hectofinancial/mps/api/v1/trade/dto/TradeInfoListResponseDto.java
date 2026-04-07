package kr.co.hectofinancial.mps.api.v1.trade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class TradeInfoListResponseDto {
    private String custNo;
    @JsonProperty("mTrdNo")
    private String mTrdNo;
    private String trdNo;
    private String trdDt;
    private String trdTm;
    private String trdDivCd;
    private String chrgMeanCd;
    private String trdSumry;
    private String trdAmt;
    private String mnyAmt;
    private String pntAmt;

    @JsonProperty("waitMnyAmt")
    private String waitMnyAmt;
    private String cnclTrdAmt;
    private String cnclMnyAmt;
    private String cnclPntAmt;
    private String lastCnclDate;
    @JsonProperty("storCd")
    private String storCd;
    @JsonProperty("storNm")
    private String storNm;
    @JsonProperty("stlMId")
    @EncField
    private String stlMId;
    @JsonProperty("trdDivDtlCd")
    private String trdDivDtlCd;
    @JsonProperty("mResrvField1")
    private String mResrvField1;
    @JsonProperty("mResrvField2")
    private String mResrvField2;
    @JsonProperty("mResrvField3")
    private String mResrvField3;


    public static TradeInfoListResponseDto of (Trade trade) {
        String trdDivCd = trade.getTrdDivCd();
        return TradeInfoListResponseDto.builder()
                .custNo(trade.getMpsCustNo())
                .mTrdNo(trade.getMTrdNo())
                .trdNo(trade.getTrdNo())
                .trdDt(trade.getTrdDt())
                .trdTm(trade.getTrdTm())
                .trdDivCd(trade.getTrdDivCd())
                .chrgMeanCd(trade.getChrgMeanCd())
                .trdSumry(StringUtils.isBlank(trade.getTrdSumry()) ? "" : trade.getTrdSumry())
                .trdAmt(String.valueOf(trade.getTrdAmt()))
                .mnyAmt(String.valueOf(trade.getMnyAmt()))
                .pntAmt(String.valueOf(trade.getPntAmt()))
                .waitMnyAmt(String.valueOf(trade.getWaitMnyAmt()))
                .cnclTrdAmt(String.valueOf(trade.getCnclTrdAmt()))
                .cnclMnyAmt(String.valueOf(trade.getCnclMnyAmt()))
                .cnclPntAmt(String.valueOf(trade.getCnclPntAmt()))
                .lastCnclDate(DateTimeUtil.convertStringDateTime(trade.getLastCnclDate()))
                .stlMId(StringUtils.isBlank(trade.getStlMId()) ? "" : trade.getStlMId())
                .storCd(StringUtils.isBlank(trade.getStorCd()) ? "" : trade.getStorCd())
                .storNm(StringUtils.isBlank(trade.getStorNm()) ? "" : trade.getStorNm())
                .trdDivDtlCd(StringUtils.isBlank(trade.getTrdDivDtlCd()) ? "" : trade.getTrdDivDtlCd())
                .mResrvField1(StringUtils.isBlank(trade.getMResrvField1()) ? "" : trade.getMResrvField1())
                .mResrvField2(StringUtils.isBlank(trade.getMResrvField2()) ? "" : trade.getMResrvField2())
                .mResrvField3(StringUtils.isBlank(trade.getMResrvField3()) ? "" : trade.getMResrvField3())
                .build();
    }
}
