package kr.co.hectofinancial.mps.api.v1.card.controller;

import kr.co.hectofinancial.mps.api.v1.card.dto.*;
import kr.co.hectofinancial.mps.api.v1.card.service.CardService;
import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.NotiCardRequsetDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.service.CardNotiService;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MpsMarketDto;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.AutoChargeDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustChrgMeanDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.PointExpireRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.PointExpireResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletCancelResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.api.v1.trade.service.PointService;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto.*;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/card")
public class CardController {

    private final PointService pointService;
    private final CardService cardService;
    private final CommonService commonService;
    private final AutoChargeAmountValidService autoChargeAmountValidService;
    private final AutoChargeAvailabilityValidService autoChargeAvailabilityValidService;
    private final ShortageAutoChargeService shortageAutoChargeService;
    private final ThresholdAutoChargeService thresholdAutoChargeService;
    private final CustChrgMeanService custChrgMeanService;
    private final CardNotiService cardNotiService;
    private final TradeRepository tradeRepository;

    @PostMapping("/use")
    public ResponseEntity<BaseResponseDto> useWallet(@RequestBody CardUseApprovalRequestDto cardUseApprovalRequestDto) {

        MpsMarketDto marketDto = null;
        MarketAddInfoDto marketAddInfoDto = null;
        CustomerDto customerDto = cardUseApprovalRequestDto.getCustomerDto();
        CustChrgMeanDto custChrgMeanDto = null;

        String custNo = cardUseApprovalRequestDto.getCustNo();
        String mid = customerDto.getMid();
        long trdAmt = Long.parseLong(cardUseApprovalRequestDto.getTrdAmt());
        long pntBlc = Long.parseLong(cardUseApprovalRequestDto.getPntBlc());
        long mnyBlc = Long.parseLong(cardUseApprovalRequestDto.getMnyBlc());
        Long maxMnyBlc = customerDto.getChrgLmtAmlt();


        autoChargeAmountValidService.checkTrdAmt(trdAmt);
        autoChargeAmountValidService.checkRealBlc(custNo, trdAmt, mnyBlc, pntBlc, maxMnyBlc);

        boolean isAutoChargeMarket = autoChargeAvailabilityValidService.isAutoChargeableMarket(mid);

        //거래금액이 잔액보다 큼 => 부족분 충전 필요
        if (shortageAutoChargeService.isShortageAutoChargeNeeded(trdAmt, (mnyBlc + pntBlc))) {
            //자동충전 가능 상점
            if (isAutoChargeMarket && autoChargeAvailabilityValidService.isNamedCust(customerDto)) {
                custChrgMeanDto = custChrgMeanService.getCustChrgMeanForAutoChrg(custNo);
                //부족분 자동충전 가능 고객
                if (autoChargeAvailabilityValidService.isAutoChargeableCust(custChrgMeanDto) && custChrgMeanDto.isShortageEnabled()) {
                    //자동충전 시작

                    marketDto = commonService.getMpsMarketInfo(mid);
                    marketAddInfoDto = commonService.getMarketAddInfoByMId(mid);
                    if (StringUtils.isBlank(CommonUtil.nullTrim(cardUseApprovalRequestDto.getReqDt())) ||
                            StringUtils.isBlank(CommonUtil.nullTrim(cardUseApprovalRequestDto.getReqTm()))
                    ) {
                        CustomDateTimeUtil curDt = new CustomDateTimeUtil();
                        cardUseApprovalRequestDto.setReqDt(curDt.getDate());
                        cardUseApprovalRequestDto.setReqTm(curDt.getTime());
                    }

                    long reqAmt = trdAmt - (mnyBlc + pntBlc);
                    String postMnyBlc = shortageAutoChargeService.shortageAutoCharge(
                            AutoChargeDto.builder()
                                    .cardUseApprovalRequestDto(cardUseApprovalRequestDto)
                                    .custNo(custNo)
                                    .mTrdNo(cardUseApprovalRequestDto.getMTrdNo())
                                    .reqAmt(reqAmt)
                                    .mnyBlc(mnyBlc)
                                    .reqDt(cardUseApprovalRequestDto.getReqDt())
                                    .reqTm(cardUseApprovalRequestDto.getReqTm())
                                    .chargeMethods(custChrgMeanDto.getAutoChargeMethods())
                                    .customerDto(customerDto)
                                    .marketDto(marketDto)
                                    .marketAddInfoDto(marketAddInfoDto)
                                    .build());
                    cardUseApprovalRequestDto.setMnyBlc(postMnyBlc);
                }
            }
        }

        //사용
        CardUseApprovalResponseDto cardUseApprovalResponseDto = cardService.cardUseApproval(cardUseApprovalRequestDto);

        /* 카드 사용 노티 (HF->가맹점) */
        if (cardUseApprovalResponseDto.getTrdNo() != null) { //거래성공일때

            MarketAddInfoDto marketAddInfo = commonService.getMarketAddInfoByCustNo(cardUseApprovalResponseDto.getCustNo());
            NotiCardRequsetDto notiCardRequsetDto = NotiCardRequsetDto.builder()
                    .trdDt(cardUseApprovalResponseDto.getTrdDt()).trdNo(cardUseApprovalResponseDto.getTrdNo())
                    .encKey(marketAddInfo.getEncKey()).pktHashKey(marketAddInfo.getPktHashKey())
                    .build();
            cardNotiService.sendCardApprovalInfo(notiCardRequsetDto);
        }

        //자동충전 사용 상점 -> 기준금액 자동충전
        if (isAutoChargeMarket) {
            thresholdAutoChargeService.thresholdAutoCharge(cardUseApprovalResponseDto.getMnyBlc(), cardUseApprovalResponseDto.getMTrdNo(), custChrgMeanDto, customerDto, marketDto, marketAddInfoDto);
        }

        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(cardUseApprovalResponseDto)
                .build());
    }

    @PostMapping("/use/param")
    public ResponseEntity<BaseResponseDto> createParamUseApproval(@RequestBody CreateParamCardUseApprovalRequestDto createParamCardUseApprovalRequestDto) throws Exception {
        CreateParamCardUseApprovalResponseDto createParamCardUseApprovalResponseDto = cardService.createParamUseApproval(createParamCardUseApprovalRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(createParamCardUseApprovalResponseDto)
                .build());
    }

    @PostMapping("/use/cancel")
    public ResponseEntity<BaseResponseDto> cancelWallet(@RequestBody CardUseCancelRequestDto cardUseCancelRequestDto) {

        WalletCancelResponseDto walletCancelResponseDto = cardService.cancelWallet(cardUseCancelRequestDto);

        //포인트 만료
        PointExpireResponseDto pointExpireResponseDto = null;
        long expPntAmt = 0;
        long pntBlc = Long.parseLong(walletCancelResponseDto.getPntBlc());
        String expTrdDt = "";
        String expTrdTm = "";
        if (Long.parseLong(walletCancelResponseDto.getExpPntAmt()) > 0) {
            try {
                PointExpireRequestDto pointExpireRequestDto = PointExpireRequestDto.builder()
                        .custNo(cardUseCancelRequestDto.custNo)
                        .customerDto(cardUseCancelRequestDto.getCustomerDto())
                        .expPntAmt(Long.parseLong(walletCancelResponseDto.getExpPntAmt()))
                        .cnclTrdNo(walletCancelResponseDto.getTrdNo())
                        .cnclTrdDt(walletCancelResponseDto.getTrdDt())
                        .reqDt(walletCancelResponseDto.getTrdDt())
                        .reqTm(walletCancelResponseDto.getTrdTm())
                        .orgTrdNo(cardUseCancelRequestDto.getOrgTrdNo())
                        .orgTrdDt(cardUseCancelRequestDto.getOrgTrdDt())
                        .build();
                pointExpireResponseDto = pointService.pointExpire(pointExpireRequestDto);
                expPntAmt = pointExpireResponseDto.getExpPntAmt();
                pntBlc = pointExpireResponseDto.getPntBlc();
                expTrdDt = pointExpireResponseDto.getExpTrdDt();
                expTrdTm = pointExpireResponseDto.getExpTrdTm();

            } catch (Exception e) {
                log.error("**[사용취소] 포인트 만료 에러** 회원번호: [{}], 사용취소 거래번호: [{}]", cardUseCancelRequestDto.getCustNo(), walletCancelResponseDto.getTrdNo());
            }
        }

        /* 카드 사용 노티 (HF->가맹점) */
        if (walletCancelResponseDto.getTrdNo() != null) { //거래성공일때

            //원거래 조회
            Trade orgTrade = tradeRepository.findByTrdNoAndTrdDt(cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt());

            MarketAddInfoDto marketAddInfo = commonService.getMarketAddInfoByCustNo(walletCancelResponseDto.getCustNo());
            NotiCardRequsetDto notiCardRequsetDto = NotiCardRequsetDto.builder()
                    .trdDt(walletCancelResponseDto.getTrdDt()).trdNo(walletCancelResponseDto.getTrdNo())
                    .orgTrdDt(orgTrade.getTrdDt()).orgTrdNo(orgTrade.getTrdNo())
                    .encKey(marketAddInfo.getEncKey()).pktHashKey(marketAddInfo.getPktHashKey())
                    .build();
            cardNotiService.sendCardApprovalInfo(notiCardRequsetDto);

        }
        walletCancelResponseDto.setExpTrdDt(expTrdDt);
        walletCancelResponseDto.setExpTrdTm(expTrdTm);
        walletCancelResponseDto.setExpPntAmt(String.valueOf(expPntAmt));
        walletCancelResponseDto.setPntBlc(String.valueOf(pntBlc));

        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(walletCancelResponseDto)
                .build());
    }

    @PostMapping("/use/cancel/param")
    public ResponseEntity<BaseResponseDto> createParamUseCancelApproval(@RequestBody CreateParamCardUseCancelApprovalRequestDto createParamCardUseCancelApprovalRequestDto) throws Exception {
        CreateParamCardUseCancelApprovalResponseDto createParamCardUseCancelApprovalResponseDto = cardService.createParamUseCancelApproval(createParamCardUseCancelApprovalRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(createParamCardUseCancelApprovalResponseDto)
                .build());
    }


}
