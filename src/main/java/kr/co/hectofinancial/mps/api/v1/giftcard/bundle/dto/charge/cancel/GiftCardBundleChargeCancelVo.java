package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.cancel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardBundleChargeCancelVo {

    private String dstbTrdNo;
    private String trdDt;
    private String trdTm;
    private String mReqDtm;
    private String trdAmt;
    private String mId;
    private String mTrdNo;
    private String mResrvField1; // 예비필드1
    private String mResrvField2; // 예비필드2
    private String mResrvField3; // 예비필드3
    private String trdSumry; // 거래적요
    private String orgDstbTrdNo;
    private String orgTrdDt;
    private String gcDstbNo;
    private LocalDateTime createDate;
}
