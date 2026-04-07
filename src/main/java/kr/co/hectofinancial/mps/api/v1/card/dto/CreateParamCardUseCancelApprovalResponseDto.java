package kr.co.hectofinancial.mps.api.v1.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateParamCardUseCancelApprovalResponseDto {
    private String reqNo;
    private String custNo;
    @JsonProperty("mTrdNo")
    private String mTrdNo;
    private String reqDt;
    private String reqTm;
    private String trdAmt;
    private String cnclMnyAmt;
    private String cnclPntAmt;
    private String mnyBlc;
    private String pntBlc;
    private String orgTrdDt;
    private String orgTrdNo;
    private String pktHash;
    private String storNm;
    private String storCd;
    @JsonProperty("mResrvField1")
    private String mResrvField1;
    @JsonProperty("mResrvField2")
    private String mResrvField2;
    @JsonProperty("mResrvField3")
    private String mResrvField3;
    private String cardMngNo;
    private String trdSumry;

}
