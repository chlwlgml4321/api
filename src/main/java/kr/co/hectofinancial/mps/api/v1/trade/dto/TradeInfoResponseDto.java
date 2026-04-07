package kr.co.hectofinancial.mps.api.v1.trade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.constant.StatusCd;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class TradeInfoResponseDto {
    private String custNo;
    private String mTrdNo;
    private String trdNo;
    private String orgTrdNo;
    private String reqDt;
    private String reqTm;
    private String trdDt;
    private String trdTm;
    private String trdDivCd;
    private String chrgMeanCd;
    private String trdSumry;
    private String trdAmt;
    private String mnyAmt;
    private String mnyVldPd;
    private String mnyBlc;
    private String pntAmt;
    private String pntVldPd;
    private String pntBlc;
    private String waitMnyAmt;
    private String waitMnyBlc;
    private String csrcIssReqYn;
    private String csrcIssStatCd;
    private String csrcApprNo;
    private String csrcApprDtm;
    private String cnclYn;
    @JsonProperty("storCd")
    private String storCd;
    @JsonProperty("storNm")
    private String storNm;
    @JsonProperty("stlMId")
    @EncField
    private String stlMId;
    private String cnclTrdAmt;
    private String cnclMnyAmt;
    private String cnclPntAmt;
    private String lastCnclDate;
    private String status;
    @JsonProperty("trdDivDtlCd")
    private String trdDivDtlCd;
    @JsonProperty("mResrvField1")
    private String mResrvField1;
    @JsonProperty("mResrvField2")
    private String mResrvField2;
    @JsonProperty("mResrvField3")
    private String mResrvField3;

    public static TradeInfoResponseDto of(Trade trade, String vldPd) {
        /* vldPd = trdDivCd 에 따른 머니유효기간, 포인트유효기간*/

        String trdDivCd = trade.getTrdDivCd();
        return TradeInfoResponseDto.builder()
                .custNo(trade.getMpsCustNo())
                .mTrdNo(trade.getMTrdNo())
                .trdNo(trade.getTrdNo())
                .orgTrdNo(StringUtils.isBlank(trade.getOrgTrdNo()) ? "" : trade.getOrgTrdNo())
                .reqDt(trade.getMReqDtm().substring(0, 8))
                .reqTm(trade.getMReqDtm().substring(8))
                .trdDt(trade.getTrdDt())
                .trdTm(trade.getTrdTm())
                .trdDivCd(trade.getTrdDivCd())
                .chrgMeanCd(StringUtils.isBlank(trade.getChrgMeanCd()) ? "" : trade.getChrgMeanCd())
                .trdSumry(StringUtils.isBlank(trade.getTrdSumry()) ? "" : trade.getTrdSumry())
                .trdAmt(String.valueOf(trade.getTrdAmt()))
                .mnyAmt(String.valueOf(trade.getMnyAmt()))
                .mnyBlc(String.valueOf(trade.getMnyBlc()))
                .pntAmt(String.valueOf(trade.getPntAmt()))
                .pntBlc(String.valueOf(trade.getPntBlc()))
                .mnyVldPd(trade.getTrdDivCd().equals(TrdDivCd.MONEY_PROVIDE.getTrdDivCd()) ? vldPd : "")
                .pntVldPd(trade.getTrdDivCd().equals(TrdDivCd.POINT_PROVIDE.getTrdDivCd()) ? vldPd : "")
                .waitMnyBlc(String.valueOf(trade.getWaitMnyBlc()))
                .waitMnyAmt(String.valueOf(trade.getWaitMnyAmt()))
                .csrcIssReqYn(trade.getCsrcIssReqYn())
                .csrcIssStatCd(StringUtils.isBlank(trade.getCsrcIssStatCd()) ? "" : trade.getCsrcIssStatCd())
                .csrcApprNo(StringUtils.isBlank(trade.getCsrcApprNo()) ? "" : trade.getCsrcApprNo())
                .csrcApprDtm(StringUtils.isBlank(trade.getCsrcApprDtm()) ? "" : trade.getCsrcApprDtm())
                .cnclYn(trade.getCnclYn())
                .stlMId(StringUtils.isBlank(trade.getStlMId()) ? "" : trade.getStlMId())
                .storCd(StringUtils.isBlank(trade.getStorCd()) ? "" : trade.getStorCd())
                .storNm(StringUtils.isBlank(trade.getStorNm()) ? "" : trade.getStorNm())
                .cnclTrdAmt(String.valueOf(trade.getCnclTrdAmt()))
                .cnclMnyAmt(String.valueOf(trade.getCnclMnyAmt()))
                .cnclPntAmt(String.valueOf(trade.getCnclPntAmt()))
                .lastCnclDate(DateTimeUtil.convertStringDateTime(trade.getLastCnclDate()))
                .trdDivDtlCd(StringUtils.isBlank(trade.getTrdDivDtlCd()) ? "" : trade.getTrdDivDtlCd())
                .mResrvField1(StringUtils.isBlank(trade.getMResrvField1()) ? "" : trade.getMResrvField1())
                .mResrvField2(StringUtils.isBlank(trade.getMResrvField2()) ? "" : trade.getMResrvField2())
                .mResrvField3(StringUtils.isBlank(trade.getMResrvField3()) ? "" : trade.getMResrvField3())
                .status(TrdDivCd.isWithdrawCode(trdDivCd) && StringUtils.isBlank(trade.getChrgTrdNo()) ? StatusCd.PROCESSING.getStatusCd() : StatusCd.COMPLETED.getStatusCd())
                .build();
    }

}
