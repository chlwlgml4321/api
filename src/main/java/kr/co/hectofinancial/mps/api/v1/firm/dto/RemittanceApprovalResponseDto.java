package kr.co.hectofinancial.mps.api.v1.firm.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class RemittanceApprovalResponseDto {

    private String outStatCd; //성공: 0021, 실패: 0031
    private String outRsltCd; //거절코드
    private String outRsltMsg;
    private String mchtTrdNo; //상점주문번호
    private String trdNo; //서버 거래번호
    private Number trdAmt;
    private Number svcDivCd; //송금시 사용된 서비스 ex) 1.펌뱅킹
}
