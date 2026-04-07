package kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto;

import kr.co.hectofinancial.mps.global.constant.AutoChargeType;
import kr.co.hectofinancial.mps.global.constant.TrBlcUseOrd;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import lombok.*;
@ToString
@Getter
public class PgPayRequestDto {

    @NonNull
    private String mTrdNo; //가맹점 거래번호
    @NonNull
    private String reqDt; //요청 일자
    @NonNull
    private String reqTm; //요청 시각
    @NonNull
    private long reqAmt; //요청 금액 (결제금액)
    @NonNull
    private long blcAmt; //현재 잔액
    @NonNull
    private AutoChargeType autoChargeType; //자동충전타입
    private TrdChrgMeanCd chrgMeanCd; //충전 수단 코드
    private String bankCd; //은행코드
    private String custAcntKey; //헥토계좌번호키
    private String custAcntSuffix; //계좌번호 뒷 세자리
    private String fnCd; //카드사구분코드 (todo 아직 사용안함)
    private String cardKey; //헥토카드번호키 (todo 아직 사용안함)
    private String cardSuffix; //카드번호 뒷 세자리 (todo 아직 사용안함)
    @NonNull
    private String encKey; //상점암호화키
    @NonNull
    private String pktHashKey; //상점해시키
    @Builder.Default private String prdtNm = "안심선불충전"; //상품명
    @Builder.Default private String sumry = "안심선불충전"; //고객 계좌적요
    /* 충전 및 거래내역 저장 시 사용 */
    @Builder.Default private final String trdDivCd = TrdDivCd.MONEY_PROVIDE.getTrdDivCd(); //머니충전 MP 고정
    @Builder.Default private final String blcUseOrd = TrBlcUseOrd.MONEY.getBlcUseOrd(); //머니고정
    @Builder.Default private final String trdSumry = "머니 자동충전"; //안심선불 거래적요
    @NonNull
    private String mid; //상점아이디
    @NonNull
    private String mpsCustNo; //선불고객번호
    @NonNull
    private String mCustId; //선불고객아이디
    @NonNull
    private String mchtCustId; //상점고객아이디
    private long custBdnFeeAmt; //고객수수료금액
    private String trdDivDtlCd; //거래구분상세코드
    private String mResrvField1; //상점여유필드1
    private String mResrvField2; //상점여유필드2
    private String mResrvField3; //상점여유필드3
    private int tryCnt; //시도회차

    @Builder
    public PgPayRequestDto(@NonNull String mTrdNo, @NonNull long reqAmt, @NonNull String encKey, @NonNull String pktHashKey, @NonNull String mid, @NonNull String mpsCustNo, @NonNull String mCustId, @NonNull String mchtCustId, @NonNull long blcAmt, @NonNull AutoChargeType autoChargeType, String reqDt, String reqTm) {
        this.mTrdNo = mTrdNo;
        this.reqAmt = reqAmt;
        this.encKey = encKey;
        this.pktHashKey = pktHashKey;
        this.mid = mid;
        this.mpsCustNo = mpsCustNo;
        this.mCustId = mCustId;
        this.mchtCustId = mchtCustId;
        this.blcAmt = blcAmt;
        this.autoChargeType = autoChargeType;
        this.reqDt = reqDt;
        this.reqTm = reqTm;
    }

    public void setCardInfo(String code, String key, String suffix) {
        this.chrgMeanCd = TrdChrgMeanCd.CREDITCARD_APPROVAL;
        this.fnCd = code;
        this.cardKey = key;
        this.cardSuffix = suffix;
        this.custBdnFeeAmt = 0l;
    }

    public void setBankAccountInfo(String code, String key, String suffix) {
        this.chrgMeanCd = TrdChrgMeanCd.RP;
        this.bankCd = code;
        this.custAcntKey = key;
        this.custAcntSuffix = suffix;
        this.custBdnFeeAmt = 0l;
    }

    /**
     * 고객통장적요 (출금계좌) 혹은 카드사용처에 찍힐 내용 수정 (기본값은 안심선불충전)
     * @param customSumry
     */
    public void setCustTrdSumry(String customSumry) {
        this.sumry = customSumry;
        this.prdtNm = customSumry;
    }
    public void setTrdDivDtlCd(String trdDivDtlCd) {
        this.trdDivDtlCd = trdDivDtlCd;
    }

    public void setmResrvField1(String mResrvField1) {
        this.mResrvField1 = mResrvField1;
    }

    public void setmResrvField2(String mResrvField2) {
        this.mResrvField2 = mResrvField2;
    }

    public void setmResrvField3(String mResrvField3) {
        this.mResrvField3 = mResrvField3;
    }

    public void setTryCnt(int tryCnt) {
        this.tryCnt = tryCnt;
    }

    public void setBlcAmt(long blcAmt) {
        this.blcAmt = blcAmt;
    }

    public void setReqAmt(long reqAmt) {
        this.reqAmt = reqAmt;
    }

}
