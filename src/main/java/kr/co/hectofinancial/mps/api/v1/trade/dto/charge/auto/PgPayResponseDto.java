package kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto;

import kr.co.hectofinancial.mps.global.constant.AutoChargeType;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import lombok.*;

/**
 * PG연동 후 응답객체
 */
@Getter
@Builder
@ToString
public class PgPayResponseDto {
    private String resultCode;
    private String resultMsg;
    private String trdNo;
    private String trdDt;
    private String trdTm;
    private String chrgDtm;
    private String chrgTrdNo;
    private String mTrdNo; //가맹점 거래번호
    private String reqDt; //요청 일자
    private String reqTm; //요청 시각
    private long reqAmt; //요청 금액 (결제금액)
    private long preMnyBlc; //충전 전 잔액
    private long postMnyBlc; //충전 후 잔액
    private String bankCd; //은행코드
    private String custAcntKey; //헥토계좌번호키
    private String custAcntSuffix; //계좌번호 뒷 세자리
    private String fnCd; //카드사구분코드
    private String cardKey; //헥토카드번호키 (todo 아직 사용안함)
    private String cardSuffix; //카드번호 뒷 세자리 (todo 아직 사용안함)
    private String encKey; //상점암호화키
    private String pktHashKey; //상점해시키
    private String mid; //상점아이디
    private String mpsCustNo; //선불고객번호
    private String mCustId; //선불고객아이디
    private String mchtCustId; //mps_m 의 pinVrfyTypeCd 에 따라 mpsCustNo 이거나, mCustId
    private int tryCnt; //시도회차
    private AutoChargeType autoChargeType; //자동충전타입
    private TrdChrgMeanCd chrgMeanCd; //충전 수단 코드

    public static PgPayResponseDto failResponse(PgPayRequestDto pgPayRequestDto, ErrorCode errorCode) {
        return failResponse(pgPayRequestDto, errorCode.getErrorCode(), errorCode.getErrorMessage());
    }
    public static PgPayResponseDto failResponse(PgPayRequestDto pgPayRequestDto, String errorCd, String errorMsg) {
        return PgPayResponseDto.builder()
                .resultCode(errorCd)
                .resultMsg(errorMsg)
//                .trdNo()
//                .trdDt()
//                .chrgTrdNo()
                .mTrdNo(pgPayRequestDto.getMTrdNo())
                .reqDt(pgPayRequestDto.getReqDt())
                .reqTm(pgPayRequestDto.getReqTm())
                .reqAmt(pgPayRequestDto.getReqAmt())
//                .preMnyBlc()
//                .postMnyBlc()
                .bankCd(pgPayRequestDto.getBankCd())
                .custAcntKey(pgPayRequestDto.getCustAcntKey())
                .custAcntSuffix(pgPayRequestDto.getCustAcntSuffix())
                .fnCd(pgPayRequestDto.getFnCd())
                .cardKey(pgPayRequestDto.getCardKey())
                .cardSuffix(pgPayRequestDto.getCardSuffix())
                .encKey(pgPayRequestDto.getEncKey())
                .pktHashKey(pgPayRequestDto.getPktHashKey())
                .mid(pgPayRequestDto.getMid())
                .mpsCustNo(pgPayRequestDto.getMpsCustNo())
                .mCustId(pgPayRequestDto.getMCustId())
                .mchtCustId(pgPayRequestDto.getMchtCustId())
                .tryCnt(pgPayRequestDto.getTryCnt())
                .autoChargeType(pgPayRequestDto.getAutoChargeType())
                .chrgMeanCd(pgPayRequestDto.getChrgMeanCd())
                .build();
    }
}
