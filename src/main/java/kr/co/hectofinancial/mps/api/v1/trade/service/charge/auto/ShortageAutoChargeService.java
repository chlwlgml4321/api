package kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto;

import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletUseEachRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.service.wallet.UseFailService;
import kr.co.hectofinancial.mps.global.constant.AutoChargeType;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.AutoChargeDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustAutoChargeMethod;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.PgPayRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.PgPayResponseDto;
import kr.co.hectofinancial.mps.api.v1.card.dto.CardUseApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MpsMarketDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeFailInsertDto;
import kr.co.hectofinancial.mps.global.constant.TrBlcUseOrd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortageAutoChargeService {
    private final PgPayService pgPayService;
    private final UseFailService useFailService;

    public boolean isShortageAutoChargeNeeded(long trdAmt, long blc) {
        if (trdAmt > blc) {
            log.info("[isShortageAutoChargeNeeded TRUE] 잔액부족으로 사용전 자동충전 필요! trdAmt={} blc={}", trdAmt, blc);
            return true;
        }
        log.info("[isShortageAutoChargeNeeded FALSE] 잔액충분으로 사용전 자동충전 불필요! trdAmt={} blc={}", trdAmt, blc);
        return false;
    }

    /**
     * 부족분 자동충전 후 머니 잔액 반환
     */
    public String shortageAutoCharge(AutoChargeDto autoChargeDto) {

        String custNo = autoChargeDto.getCustNo();
        String mTrdNo = autoChargeDto.getMTrdNo();
        long reqAmt = autoChargeDto.getReqAmt();
        long mnyBlc = autoChargeDto.getMnyBlc();
        String reqDt = autoChargeDto.getReqDt();
        String reqTm = autoChargeDto.getReqTm();
        List<CustAutoChargeMethod> chargeMethods = autoChargeDto.getChargeMethods();
        CustomerDto customerDto = autoChargeDto.getCustomerDto();
        MpsMarketDto marketDto = autoChargeDto.getMarketDto();
        MarketAddInfoDto marketAddInfoDto = autoChargeDto.getMarketAddInfoDto();

        long formattedReqAmt = CommonUtil.calculateChargeAmout(reqAmt, 10000L, 10000L);//충전금액 최소충전금액 비교 계산
        String mchtCustId = "C".equals(marketDto.getPinVrifyTypeCd()) ? customerDto.getMCustId() : customerDto.getMpsCustNo();

        log.info("[ShortageAutoCharge] 부족분 자동충전 시작! custNo={} mTrdNo={} reqAmt={}", custNo, mTrdNo, reqAmt);
        PgPayRequestDto pgPayRequestDto = PgPayRequestDto.builder()
                .mTrdNo(mTrdNo)
                .reqAmt(formattedReqAmt)
                .encKey(marketAddInfoDto.getEncKey())
                .pktHashKey(marketAddInfoDto.getPktHashKey())
                .mid(customerDto.getMid())
                .mpsCustNo(custNo)
                .mCustId(customerDto.getMCustId())
                .mchtCustId(mchtCustId)
                .blcAmt(mnyBlc)
                .autoChargeType(AutoChargeType.SHORTAGE)
                .reqDt(reqDt)
                .reqTm(reqTm)
                .build();
        if (StringUtils.isNotBlank(CommonUtil.nullTrim(marketDto.getWdTrdSumry()))) {
            //고객 통장 적요값 상점이 설정한 값으로 변경
            pgPayRequestDto.setCustTrdSumry(marketDto.getWdTrdSumry());
        }
        PgPayResponseDto pgPayResponseDto = pgPayService.payAndChargeMoney(customerDto, pgPayRequestDto, chargeMethods, null);
        if ("0000".equals(pgPayResponseDto.getResultCode())) {
            String postMnyBlc = String.valueOf(pgPayResponseDto.getPostMnyBlc());
            log.info("[ShortageAutoCharge SUCCESS] 부족분 자동충전 성공! custNo={} mTrdNo={} postMnyBlc={}", custNo, mTrdNo, postMnyBlc);
            return postMnyBlc;
        }
        //사용 실패로 TrdFail 쌓고, 실패 응답 (원거래금액/요청금액을 확인하세요.)
        log.info("[ShortageAutoCharge FAIL] 부족분 자동충전 *실패!* custNo={} mTrdNo={}", custNo, mTrdNo);
        if (autoChargeDto.getWalletUseRequestDto() != null) {
            insertWalletUseFail(autoChargeDto.getWalletUseRequestDto(), customerDto);
        }else if(autoChargeDto.getWalletUseEachRequestDto() != null){
            insertWalletUseEachFail(autoChargeDto.getWalletUseEachRequestDto(), customerDto);
        } else if (autoChargeDto.getCardUseApprovalRequestDto() != null) {
            insertCardUseFail(autoChargeDto.getCardUseApprovalRequestDto(), customerDto);
        }
        throw new RequestValidationException(ErrorCode.REQ_AMT_NOT_MATCHED);
    }

    private void insertWalletUseFail(WalletUseRequestDto walletUseRequestDto, CustomerDto customerDto) {
        useFailService.createUseTradeFail(TradeFailInsertDto.builder()
                .trdAmt(Long.parseLong(walletUseRequestDto.getTrdAmt()))
                .mnyBlc(Long.parseLong(walletUseRequestDto.getMnyBlc()))
                .pntBlc(Long.parseLong(walletUseRequestDto.getPntBlc()))
                .mTrdNo(walletUseRequestDto.getMTrdNo())
                .blcUseOrd(StringUtils.isBlank(walletUseRequestDto.getBlcUseOrd()) ? "P" : walletUseRequestDto.getBlcUseOrd())
                .reqDtm(walletUseRequestDto.getReqDt() + walletUseRequestDto.getReqTm())
                .csrcIssReqYn(walletUseRequestDto.getCsrcIssReqYn())
                .stlMId(walletUseRequestDto.getStlMId())
                .storCd(walletUseRequestDto.getStorCd())
                .storNm(walletUseRequestDto.getStorNm())
                .mResrvField1(walletUseRequestDto.getMResrvField1())
                .mResrvField2(walletUseRequestDto.getMResrvField2())
                .mResrvField3(walletUseRequestDto.getMResrvField3())
                .build(), customerDto);
    }
    private void insertWalletUseEachFail(WalletUseEachRequestDto walletUseEachRequestDto, CustomerDto customerDto) {
        long pntAmt = Long.parseLong(walletUseEachRequestDto.getPntAmt());
        long mnyAmt = Long.parseLong(walletUseEachRequestDto.getMnyAmt());

        useFailService.createUseTradeFail(TradeFailInsertDto.builder()
                .trdAmt(pntAmt + mnyAmt)
                .mnyAmt(mnyAmt)
                .pntAmt(pntAmt)
                .blcUseOrd(TrBlcUseOrd.POINT.getBlcUseOrd())
                .mnyBlc(Long.parseLong(walletUseEachRequestDto.getMnyBlc()))
                .pntBlc(Long.parseLong(walletUseEachRequestDto.getPntBlc()))
                .mTrdNo(walletUseEachRequestDto.getMTrdNo())
                .reqDtm(walletUseEachRequestDto.getReqDt() + walletUseEachRequestDto.getReqTm())
                .csrcIssReqYn(walletUseEachRequestDto.getCsrcIssReqYn())
                .stlMId(walletUseEachRequestDto.getStlMId())
                .storCd(walletUseEachRequestDto.getStorCd())
                .storNm(walletUseEachRequestDto.getStorNm())
                .mResrvField1(walletUseEachRequestDto.getMResrvField1())
                .mResrvField2(walletUseEachRequestDto.getMResrvField2())
                .mResrvField3(walletUseEachRequestDto.getMResrvField3())
                .build(), customerDto);
    }
    private void insertCardUseFail(CardUseApprovalRequestDto cardUseApprovalRequestDto, CustomerDto customerDto) {
        useFailService.createUseTradeFail(TradeFailInsertDto.builder()
                .trdAmt(Long.parseLong(cardUseApprovalRequestDto.getTrdAmt()))
                .mnyBlc(Long.parseLong(cardUseApprovalRequestDto.getMnyBlc()))
                .pntBlc(Long.parseLong(cardUseApprovalRequestDto.getPntBlc()))
                .mTrdNo(cardUseApprovalRequestDto.getMTrdNo())
                .reqDtm(cardUseApprovalRequestDto.getReqDt() + cardUseApprovalRequestDto.getReqTm())
                .csrcIssReqYn(cardUseApprovalRequestDto.getCsrcIssReqYn())
                .blcUseOrd(StringUtils.isBlank(cardUseApprovalRequestDto.getBlcUseOrd()) ? "P" : cardUseApprovalRequestDto.getBlcUseOrd())
                .stlMId(cardUseApprovalRequestDto.getStlMId())
                .storCd(cardUseApprovalRequestDto.getStorCd())
                .storNm(cardUseApprovalRequestDto.getStorNm())
                .mResrvField1(cardUseApprovalRequestDto.getMResrvField1())
                .mResrvField2(cardUseApprovalRequestDto.getMResrvField2())
                .mResrvField3(cardUseApprovalRequestDto.getMResrvField3())
                .build(), customerDto);
    }
}
