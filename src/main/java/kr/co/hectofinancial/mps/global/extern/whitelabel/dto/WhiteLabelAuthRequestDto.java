package kr.co.hectofinancial.mps.global.extern.whitelabel.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 화이트라벨 계좌 출금 인증 API 연동 용 RequestDto
 */
@Getter
@Builder
public class WhiteLabelAuthRequestDto {
    private Params params;
    private Datas data;

    @Getter
    @Builder
    public static class Params {
        private String mchtId;    //상점아이디
        @Builder.Default
        private String ver = "0A19";    //버전
        @Builder.Default
        private String method = "WL";    //결제수단코드
        @Builder.Default
        private String bizType = "B1";    //업무구분코드
        @Builder.Default
        private String encCd = "23";    //암호화구분코드
        private String mchtTrdNo;    //상점주문번호
        private String trdDt;    //거래요청일자
        private String trdTm;    //거래요청시간
        private String mobileYn;    //모바일여부
        private String osType;    //OS구분
    }

    @Getter
    @Builder
    public static class Datas {
        private String pktHash;    //해쉬값
        private String authTrdNo;    //인증거래번호
        private String mchtCustId;    //고객아이디
        private String pmtPrdtNm;    //결제상품명
        private String bankCd;    //출금은행코드
        private String custAcntNo;    //출금계좌번호
        private String taxTypeCd;    //과세구분코드
        private String trdAmt;    //거래금액
        private String splAmt;    //공급가액
        private String vat;    //부가세
        private String svcAmt;    //봉사료
        private String sumry;    //고객계좌적요
        private String custAcntKey;    //헥토계좌번호키
        private String csrcIssReqYn;    //현금영수증발행여부
        private String csrcIssPsblYn;    //현금영수증발행가능여부
        private String csrcSelfIssYn;    //현금영수증 자진발급여부
        private String csrcRegProposYn;    //현금영수증용도구분
        private String csrcRegNoDivCd;    //현금영수증발행정보구분
        private String csrcRegNo;    //현금영수증발행정보
        private String addDdtTypeCd;    //추가공제구분코드
        private String trdPurDiv;    //출금거래목적구분
        private String cupDepositAmt;    //자원순환보증금
        private String ornTrdAmt;    //간편현금거래금액
        private String DcAmt;    //즉시할인금액
    }

}
