package kr.co.hectofinancial.mps.api.v1.trade.controller;

import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.AutoChargeDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustChrgMeanDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.WithdrawalMoneyRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.WithdrawalMoneyResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.PointExpireRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.PointExpireResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.*;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto.AutoChargeAmountValidService;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto.AutoChargeAvailabilityValidService;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto.ShortageAutoChargeService;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto.ThresholdAutoChargeService;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto.CustChrgMeanService;
import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MpsMarketDto;
import kr.co.hectofinancial.mps.api.v1.trade.service.PointService;
import kr.co.hectofinancial.mps.api.v1.trade.service.wallet.WalletService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import kr.co.hectofinancial.mps.global.util.CustomerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/wallet")
public class WalletController {

    private final WalletService walletService;
    private final PointService pointService;
    private final CommonService commonService;
    private final AutoChargeAmountValidService autoChargeAmountValidService;
    private final AutoChargeAvailabilityValidService autoChargeAvailabilityValidService;
    private final ShortageAutoChargeService shortageAutoChargeService;
    private final ThresholdAutoChargeService thresholdAutoChargeService;
    private final CustChrgMeanService custChrgMeanService;

    @PostMapping("/use")
    public ResponseEntity<BaseResponseDto> useWallet(@RequestBody WalletUseRequestDto walletUseRequestDto) {

        MpsMarketDto marketDto = null;
        MarketAddInfoDto marketAddInfoDto = null;
        CustomerDto customerDto = walletUseRequestDto.getCustomerDto();
        CustChrgMeanDto custChrgMeanDto = null;

        String custNo = walletUseRequestDto.getCustNo();
        String mid = customerDto.getMid();
        long trdAmt = Long.parseLong(walletUseRequestDto.getTrdAmt());
        long pntBlc = Long.parseLong(walletUseRequestDto.getPntBlc());
        long mnyBlc = Long.parseLong(walletUseRequestDto.getMnyBlc());
        long maxMnyBlc = customerDto.getChrgLmtAmlt();


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
                    if (StringUtils.isBlank(CommonUtil.nullTrim(walletUseRequestDto.getReqDt())) ||
                            StringUtils.isBlank(CommonUtil.nullTrim(walletUseRequestDto.getReqTm()))
                    ) {
                        CustomDateTimeUtil curDt = new CustomDateTimeUtil();
                        walletUseRequestDto.setReqDt(curDt.getDate());
                        walletUseRequestDto.setReqTm(curDt.getTime());
                    }

                    long reqAmt = trdAmt - (mnyBlc + pntBlc);
                    String postMnyBlc = shortageAutoChargeService.shortageAutoCharge(
                            AutoChargeDto.builder()
                                    .walletUseRequestDto(walletUseRequestDto)
                                    .custNo(custNo)
                                    .mTrdNo(walletUseRequestDto.getMTrdNo())
                                    .reqAmt(reqAmt)
                                    .mnyBlc(mnyBlc)
                                    .reqDt(walletUseRequestDto.getReqDt())
                                    .reqTm(walletUseRequestDto.getReqTm())
                                    .chargeMethods(custChrgMeanDto.getAutoChargeMethods())
                                    .customerDto(customerDto)
                                    .marketDto(marketDto)
                                    .marketAddInfoDto(marketAddInfoDto)
                                    .build());
                    walletUseRequestDto.setMnyBlc(postMnyBlc);
                }
            }
        }
        //사용
        WalletUseResponseDto walletUseResponseDto = walletService.useWallet(walletUseRequestDto);
        //자동충전 사용 상점 -> 기준금액 자동충전
        if (isAutoChargeMarket) {
            thresholdAutoChargeService.thresholdAutoCharge(walletUseResponseDto.getMnyBlc(), walletUseResponseDto.getMTrdNo(), custChrgMeanDto, customerDto, marketDto, marketAddInfoDto);
        }

        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(walletUseResponseDto)
                .build());
    }

    @PostMapping("/use/cancel")
    public ResponseEntity<BaseResponseDto> cancelWallet(@RequestBody WalletCancelRequestDto walletCancelRequestDto) {

        WalletCancelResponseDto walletCancelResponseDto = walletService.cancelWallet(walletCancelRequestDto);

        //포인트 만료
        PointExpireResponseDto pointExpireResponseDto = null;
        long expPntAmt = 0;
        long pntBlc = Long.parseLong(walletCancelResponseDto.getPntBlc());
        String expTrdDt = "";
        String expTrdTm = "";
        if (Long.parseLong(walletCancelResponseDto.getExpPntAmt()) > 0) {
            try {
                PointExpireRequestDto pointExpireRequestDto = PointExpireRequestDto.builder()
                        .custNo(walletCancelRequestDto.custNo)
                        .customerDto(walletCancelRequestDto.getCustomerDto())
                        .expPntAmt(Long.parseLong(walletCancelResponseDto.getExpPntAmt()))
                        .cnclTrdNo(walletCancelResponseDto.getTrdNo())
                        .cnclTrdDt(walletCancelResponseDto.getTrdDt())
                        .reqDt(walletCancelResponseDto.getTrdDt())
                        .reqTm(walletCancelResponseDto.getTrdTm())
                        .orgTrdNo(walletCancelRequestDto.getOrgTrdNo())
                        .orgTrdDt(walletCancelRequestDto.getOrgTrdDt())
                        .build();
                pointExpireResponseDto = pointService.pointExpire(pointExpireRequestDto);
                expPntAmt = pointExpireResponseDto.getExpPntAmt();
                pntBlc = pointExpireResponseDto.getPntBlc();
                expTrdDt = pointExpireResponseDto.getExpTrdDt();
                expTrdTm = pointExpireResponseDto.getExpTrdTm();

            } catch (Exception e) {
                log.error("**[사용취소] 포인트 만료 에러** 회원번호: [{}], 사용취소 거래번호: [{}]", walletCancelRequestDto.getCustNo(), walletCancelResponseDto.getTrdNo());
            }
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

    @PostMapping("/balance")
    public ResponseEntity<BaseResponseDto> getWalletBalanceByCustNo(@RequestBody WalletBalanceRequestDto walletBalanceRequestDto) {
        CustomerUtil.checkValidCustomerByCi(walletBalanceRequestDto.getCi(), walletBalanceRequestDto.getCustomerDto());
        WalletBalanceResponseDto walletBalanceResponseDto = walletService.getWalletBalanceByCustNo(walletBalanceRequestDto.getCustNo());
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(walletBalanceResponseDto)
                .build());
    }

    @PostMapping("/balance/withdrawal")
    public ResponseEntity<BaseResponseDto> getWithdrawalMoneyAmt(@Valid @RequestBody WithdrawalMoneyRequestDto withdrawalMoneyRequestDto) {
        CustomerUtil.checkValidCustomerByCi(withdrawalMoneyRequestDto.getCi(), withdrawalMoneyRequestDto.getCustomerDto());
        WithdrawalMoneyResponseDto withdrawalMoneyResponseDto = walletService.getWithdrawalMoneyAmt(withdrawalMoneyRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(withdrawalMoneyResponseDto)
                .build());
    }

    @PostMapping("/balance/detail")
    public ResponseEntity<BaseResponseDto> getWalletBalanceDtl(@Valid @RequestBody WalletBalanceRequestDto walletBalanceRequestDto) {
        CustomerUtil.checkValidCustomerByCi(walletBalanceRequestDto.getCi(), walletBalanceRequestDto.getCustomerDto());
        CustomerWalletDtlResponseDto customerWalletDtlResponseDto = walletService.getWalletBalanceDtl(walletBalanceRequestDto.getCustNo());
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(customerWalletDtlResponseDto)
                .build());
    }


    @PostMapping("/option/balance")
    public ResponseEntity<BaseResponseDto> getWalletBalanceForAutoCharge(@Valid @RequestBody WalletOptionBalanceRequestDto walletOptionBalanceRequestDto) {

        CustomerUtil.checkValidCustomerByCi(walletOptionBalanceRequestDto.getCi(), walletOptionBalanceRequestDto.getCustomerDto());
        WalletOptionBalanceResponseDto walletBalanceForAutoCharge = walletService.getWalletOptionBalance(walletOptionBalanceRequestDto);

        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(walletBalanceForAutoCharge)
                .build());
    }

    @PostMapping("/use/each")
    public ResponseEntity<BaseResponseDto> useEachWallet(@RequestBody WalletUseEachRequestDto walletUseEachRequestDto) {

        MpsMarketDto marketDto = null;
        MarketAddInfoDto marketAddInfoDto = null;
        CustomerDto customerDto = walletUseEachRequestDto.getCustomerDto();
        CustChrgMeanDto custChrgMeanDto = null;

        String custNo = walletUseEachRequestDto.getCustNo();
        String mid = customerDto.getMid();
        long mnyAmt = Long.parseLong(walletUseEachRequestDto.getMnyAmt());
        long pntAmt = Long.parseLong(walletUseEachRequestDto.getPntAmt());
        long pntBlc = Long.parseLong(walletUseEachRequestDto.getPntBlc());
        long mnyBlc = Long.parseLong(walletUseEachRequestDto.getMnyBlc());
        Long maxMnyBlc = customerDto.getChrgLmtAmlt();


        autoChargeAmountValidService.checkTrdAmt((mnyAmt+pntAmt));
        autoChargeAmountValidService.checkRealBlc(custNo, (mnyAmt+pntAmt), mnyBlc, pntBlc, maxMnyBlc);
        autoChargeAmountValidService.checkPntAmt(pntAmt, pntBlc);

        boolean isAutoChargeMarket = autoChargeAvailabilityValidService.isAutoChargeableMarket(mid);

        //거래금액이 잔액보다 큼 => 부족분 충전 필요
        if (shortageAutoChargeService.isShortageAutoChargeNeeded(mnyAmt, mnyBlc)) {
            //자동충전 가능 상점
            if (isAutoChargeMarket && autoChargeAvailabilityValidService.isNamedCust(customerDto)) {
                custChrgMeanDto = custChrgMeanService.getCustChrgMeanForAutoChrg(custNo);
                //부족분 자동충전 가능 고객
                if (autoChargeAvailabilityValidService.isAutoChargeableCust(custChrgMeanDto) && custChrgMeanDto.isShortageEnabled()) {
                    //자동충전 시작
                    marketDto = commonService.getMpsMarketInfo(mid);
                    marketAddInfoDto = commonService.getMarketAddInfoByMId(mid);

                    if (StringUtils.isBlank(CommonUtil.nullTrim(walletUseEachRequestDto.getReqDt())) ||
                            StringUtils.isBlank(CommonUtil.nullTrim(walletUseEachRequestDto.getReqTm()))
                    ) {
                        CustomDateTimeUtil curDt = new CustomDateTimeUtil();
                        walletUseEachRequestDto.setReqDt(curDt.getDate());
                        walletUseEachRequestDto.setReqTm(curDt.getTime());
                    }

                    long reqAmt = mnyAmt - mnyBlc;
                    String postMnyBlc = shortageAutoChargeService.shortageAutoCharge(
                            AutoChargeDto.builder()
                                    .walletUseEachRequestDto(walletUseEachRequestDto)
                                    .custNo(custNo)
                                    .mTrdNo(walletUseEachRequestDto.getMTrdNo())
                                    .reqAmt(reqAmt)
                                    .mnyBlc(mnyBlc)
                                    .reqDt(walletUseEachRequestDto.getReqDt())
                                    .reqTm(walletUseEachRequestDto.getReqTm())
                                    .chargeMethods(custChrgMeanDto.getAutoChargeMethods())
                                    .customerDto(customerDto)
                                    .marketDto(marketDto)
                                    .marketAddInfoDto(marketAddInfoDto)
                                    .build());
                    walletUseEachRequestDto.setMnyBlc(postMnyBlc);
                }
            }
        }
        //사용
        WalletUseEachResponseDto walletUseEachResponseDto = walletService.useEachWallet(walletUseEachRequestDto);
        //자동충전 사용 상점 -> 기준금액 자동충전
        if (isAutoChargeMarket) {
            thresholdAutoChargeService.thresholdAutoCharge(walletUseEachResponseDto.getMnyBlc(), walletUseEachResponseDto.getMTrdNo(), custChrgMeanDto, customerDto, marketDto, marketAddInfoDto);
        }

        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(walletUseEachResponseDto)
                .build());
    }

    @PostMapping("/transfer")
    public ResponseEntity<BaseResponseDto> transferWallet(@RequestBody TransferWalletRequestDto transferWalletRequestDto) throws NoSuchAlgorithmException {
        TransferWalletResponseDto transferWalletResponseDto = walletService.transferWallet(transferWalletRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(transferWalletResponseDto)
                .build());
    }

    @PostMapping("/balance/ledger")
    public ResponseEntity<BaseResponseDto> getWalletBalanceByLedger(@RequestBody WalletBalanceByLedgerRequestDto walletBalanceByLedgerRequestDto) {
        CustomerUtil.checkValidCustomerByCi(walletBalanceByLedgerRequestDto.getCi(), walletBalanceByLedgerRequestDto.getCustomerDto());
        WalletBalanceByLedgerResponseDto walletBalanceByLedgerResponseDto = walletService.getCustWalletByLedger(walletBalanceByLedgerRequestDto);
        return ResponseEntity.ok(BaseResponseDto.builder()
                .rsltCd(ErrorCode.SUCCESS.getErrorCode())
                .rsltMsg(ErrorCode.SUCCESS.getErrorMessage())
                .rsltObj(walletBalanceByLedgerResponseDto)
                .build());
    }

}
