package kr.co.hectofinancial.mps.global.extern.whitelabel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WhiteLabelPayResponseDto {
    @JsonProperty("params") private Params params;
    @JsonProperty("data") private Datas data;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Params {
        private String mchtId;    //상점아이디
        private String ver;    //버전
        private String method;    //결제수단코드
        private String bizType;    //업무구분코드
        private String encCd;    //암호화구분코드
        private String mchtTrdNo;    //상점주문번호
        private String trdNo;    //헥토파이낸셜거래번호
        private String trdDt;    //거래요청일자
        private String trdTm;    //거래요청시간
        private String outStatCd;    //거래상태코드
        private String outRsltCd;    //결과코드
        private String outRsltMsg;    //결과메시지
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Datas {
        private String pktHash;    //해쉬값
        private String mchtCustId;    //상점고객아이디
        private String authTrdNo;   //인증거래번호
        private String trdAmt;    //거래금액
        private String payMethod;    //업무구분코드
        private String fnNm;    //카드사명
        private String fnCd;    //카드사코드
        private String svcDivCd;    //간편현금연동구분
        private String phoneNo;    //휴대폰번호
    }
}
