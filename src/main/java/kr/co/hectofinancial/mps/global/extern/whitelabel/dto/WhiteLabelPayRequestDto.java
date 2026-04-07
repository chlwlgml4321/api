package kr.co.hectofinancial.mps.global.extern.whitelabel.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WhiteLabelPayRequestDto {

    private Params params;
    private Datas data;

    @Getter
    @Builder
    public static class Params {
        private String mchtId; //상점아이디
        @Builder.Default
        private String ver = "0A19"; //버전
        @Builder.Default
        private String method = "WL"; //결제수단코드
        @Builder.Default
        private String bizType = "B9"; //업무구분코드
        @Builder.Default
        private String encCd = "23"; //암호화구분코드
        private String mchtTrdNo; //상점주문번호
        private String trdDt; //거래요청일자
        private String trdTm; //거래요청시간
        private String mobileYn; //모바일여부
        private String osType; //OS구분
    }

    @Getter
    @Builder
    public static class Datas {
        private String pktHash; //해쉬값
        private String mchtCustId; //상점고객아이디
        private String authTrdNo; //인증거래번호
        private String trdAmt; //거래금액
        private String phoneNo; //휴대폰번호
        private String phoneKey; //헥토휴대폰결제키
        private String prdtNm; //상품명
    }
}
