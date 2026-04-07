package kr.co.hectofinancial.mps.test.feign;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@Data
@NoArgsConstructor
public class FeignClientRequestDto {

    private String trdDivCd;
    private String ci;
    @JsonProperty("mid")
    private String mid;
    private String custNo; //선불회원번호
    private String custId; //선불회원아이디
    private String resCustNo; //머니 선불 회원번호
    @JsonProperty("mTrdNo")
    private String mTrdNo;
    private String pktHash; //해시키
    private String chrgMeanCd; //충전수단코드
    private String trdAmt; //거래금액
    private String mnyAmt;
    private String pntAmt;
    private String divCd; //거래구분 MP,MC=머니, PP,PC=포인트
    private String blcAmt; //잔액
    private String pntVldPd; //포인트 유효기간
    private String reqDt; //요청일자
    private String reqTm; //요청시각
    private String trdSumry; //거래적요
    private String custBdnFeeAmt; //고객부담수수료
    private String mnyBlc; //머니잔액
    private String pntBlc; //포인트잔액
    private String blcUseOrd; //잔액 사용 순서
    private String csrcIssReqYn; //현금영수증 발행 여부
    private String stlMId; //정산 상점 아이디
    private String storCd; //사용처 코드
    private String storNm; //사용처 명
    private String pinNo; //결제비밀번호
    private String orgTrdNo; //원거래 승인번호
    private String orgTrdDt; //원거래 일자
    private String waitMnyBlc; //대기머니잔액
    private String cnclMnyAmt; //취소머니금액
    private String cnclPntAmt; //취소포인트금액
    private String period;
    private int size = 10;
    private int page = 1;
    private String trdNo;
    private String trdDt;
    private String chrgTrdNo;
    private String showCnclYn;
    private String blcDivCd;
    private String cardMngNo;
    private String cnclTypeCd;
    private String cardTrdOnlyYn;


    public void createNewTrdNo() {
        this.mTrdNo = ("F_" + UUID.randomUUID().toString().substring(0, 4) + "_" + (new CustomDateTimeUtil().getDateTime()));
    }
}
