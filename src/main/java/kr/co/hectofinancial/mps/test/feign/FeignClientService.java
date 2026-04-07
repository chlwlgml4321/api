package kr.co.hectofinancial.mps.test.feign;

import kr.co.hectofinancial.mps.api.v1.card.dto.CardUseApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.*;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.*;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletBalanceRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletUseEachRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletUseRequestDto;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static kr.co.hectofinancial.mps.test.feign.FeignClientUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeignClientService {
    private final MpsClientLocal mpsClientLocal;
    private final MpsClientTb mpsClientTb;
    private final MpsClientPrd mpsClientPrd;
    private final MpsClientPrdYeoksam1 mpsClientPrdYeoksam1;
    private final MpsClientPrdYeoksam2 mpsClientPrdYeoksam2;
    private final MpsClientPrdSongdo1 mpsClientPrdSongdo1;
    private final MpsClientPrdSongdo2 mpsClientPrdSongdo2;
    private final static String TB = "test";
    private final static String PRD = "prd";
    private final static String PRD_YS1 = "prd-yeoksam1";
    private final static String PRD_YS2 = "prd-yeoksam2";
    private final static String PRD_SD1 = "prd-songdo1";
    private final static String PRD_SD2 = "prd-songdo2";



    public Object useWallet(FeignClientRequestDto feignClientRequestDto, String profile) {
        String trdDivCd = TrdDivCd.COMMON_USE.getTrdDivCd();
        feignClientRequestDto.setTrdDivCd(trdDivCd);
        String encode = null;
        try {
            if (StringUtils.isNotEmpty(feignClientRequestDto.getStorNm())) {
                encode = URLEncoder.encode(feignClientRequestDto.getStorNm(), "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            log.info("URL 인코딩 오류, {} ", feignClientRequestDto.getStorNm());
        }
        feignClientRequestDto.createNewTrdNo();
        WalletUseRequestDto walletUseRequestDto =
                WalletUseRequestDto.builder()
                        .custNo(feignClientRequestDto.getCustNo())
                        .mTrdNo(feignClientRequestDto.getMTrdNo())
                        .trdAmt(getEncVal(profile, feignClientRequestDto.getTrdAmt()))
                        .mnyBlc(getEncVal(profile, feignClientRequestDto.getMnyBlc()))
                        .pntBlc(getEncVal(profile, feignClientRequestDto.getPntBlc()))
                        .blcUseOrd(feignClientRequestDto.getBlcUseOrd())
                        .reqDt(feignClientRequestDto.getReqDt())
                        .reqTm(feignClientRequestDto.getReqTm())
                        .csrcIssReqYn(feignClientRequestDto.getCsrcIssReqYn())
                        .stlMId(getEncVal(profile, feignClientRequestDto.getStlMId()))
                        .storCd(feignClientRequestDto.getStorCd())
                        .storNm(encode)
                        .pinNo(getEncVal(profile, feignClientRequestDto.getPinNo()))
                        .pktHash(makePktHash(profile, feignClientRequestDto))
                        .build();

        if (PRD.equals(profile)) {
            return decodedDto(profile, mpsClientPrd.useWallet(walletUseRequestDto), trdDivCd);
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam1.useWallet(walletUseRequestDto), trdDivCd);
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam2.useWallet(walletUseRequestDto), trdDivCd);
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo1.useWallet(walletUseRequestDto), trdDivCd);
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo2.useWallet(walletUseRequestDto), trdDivCd);
        }else if (TB.equals(profile)) {
        return decodedDto(profile, mpsClientTb.useWallet(walletUseRequestDto), trdDivCd);

        }
        return decodedDto(profile, mpsClientLocal.useWallet(walletUseRequestDto), trdDivCd);

    }


    public Object cancelUseWallet(FeignClientRequestDto feignClientRequestDto, String profile) {
        String trdDivCd = TrdDivCd.USE_COMMON_CANCEL.getTrdDivCd();
        feignClientRequestDto.setTrdDivCd(trdDivCd);
        feignClientRequestDto.createNewTrdNo();
        WalletCancelRequestDto walletCancelRequestDto =
                WalletCancelRequestDto.builder()
                        .custNo(feignClientRequestDto.getCustNo())
                        .mTrdNo(feignClientRequestDto.getMTrdNo())
                        .orgTrdNo(feignClientRequestDto.getOrgTrdNo())
                        .orgTrdDt(feignClientRequestDto.getOrgTrdDt())
                        .cnclMnyAmt(getEncVal(profile, feignClientRequestDto.getCnclMnyAmt()))
                        .cnclPntAmt(getEncVal(profile, feignClientRequestDto.getCnclPntAmt()))
                        .mnyBlc(getEncVal(profile, feignClientRequestDto.getMnyBlc()))
                        .pntBlc(getEncVal(profile, feignClientRequestDto.getPntBlc()))
                        .trdSumry(feignClientRequestDto.getTrdSumry())
                        .pktHash(makePktHash(profile, feignClientRequestDto))
                        .build();
        if (PRD.equals(profile)) {
            return decodedDto(profile, mpsClientPrd.cancelUseWallet(walletCancelRequestDto), trdDivCd);
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam1.cancelUseWallet(walletCancelRequestDto), trdDivCd);
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam2.cancelUseWallet(walletCancelRequestDto), trdDivCd);
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo1.cancelUseWallet(walletCancelRequestDto), trdDivCd);
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo2.cancelUseWallet(walletCancelRequestDto), trdDivCd);
        }else if (TB.equals(profile)) {
            return decodedDto(profile, mpsClientTb.cancelUseWallet(walletCancelRequestDto), trdDivCd);
        }
        return decodedDto(profile, mpsClientLocal.cancelUseWallet(walletCancelRequestDto), trdDivCd);
    }
    public Object useWalletEach(FeignClientRequestDto feignClientRequestDto, String profile) {
        String trdDivCd = "CUE";
        feignClientRequestDto.setTrdDivCd(trdDivCd);
        feignClientRequestDto.createNewTrdNo();
        WalletUseEachRequestDto walletUseEachRequestDto = WalletUseEachRequestDto.builder()
                .custNo(feignClientRequestDto.getCustNo())
                .mTrdNo(feignClientRequestDto.getMTrdNo())
                .mnyAmt(getEncVal(profile, feignClientRequestDto.getMnyAmt()))
                .pntAmt(getEncVal(profile, feignClientRequestDto.getPntAmt()))
                .mnyBlc(getEncVal(profile, feignClientRequestDto.getMnyBlc()))
                .pntBlc(getEncVal(profile, feignClientRequestDto.getPntBlc()))
                .reqDt(feignClientRequestDto.getReqDt())
                .reqTm(feignClientRequestDto.getReqTm())
                .csrcIssReqYn(feignClientRequestDto.getCsrcIssReqYn())
                .stlMId(getEncVal(profile, feignClientRequestDto.getStlMId()))
                .storCd(feignClientRequestDto.getStorCd())
                .storNm(feignClientRequestDto.getStorNm())
                .pinNo(getEncVal(profile, feignClientRequestDto.getPinNo()))
                .pktHash(makePktHash(profile, feignClientRequestDto))
                .build();
        if (PRD.equals(profile)) {
            return decodedDto(profile, mpsClientPrd.useWalletEach(walletUseEachRequestDto), trdDivCd);
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam1.useWalletEach(walletUseEachRequestDto), trdDivCd);
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam2.useWalletEach(walletUseEachRequestDto), trdDivCd);
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo1.useWalletEach(walletUseEachRequestDto), trdDivCd);
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo2.useWalletEach(walletUseEachRequestDto), trdDivCd);
        } else if (TB.equals(profile)) {
            return decodedDto(profile, mpsClientTb.useWalletEach(walletUseEachRequestDto), trdDivCd);
        }
        return decodedDto(profile, mpsClientLocal.useWalletEach(walletUseEachRequestDto), trdDivCd);
    }

    public Object getWalletBalance(FeignClientRequestDto feignClientRequestDto, String profile) {
        WalletBalanceRequestDto walletBalanceRequestDto
                = WalletBalanceRequestDto.builder()
                .custNo(feignClientRequestDto.getCustNo())
                .build();
        if (PRD.equals(profile)) {
            return decodedDto(mpsClientPrd.getWalletBalance(walletBalanceRequestDto));
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam1.getWalletBalance(walletBalanceRequestDto));
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam2.getWalletBalance(walletBalanceRequestDto));
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo1.getWalletBalance(walletBalanceRequestDto));
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo2.getWalletBalance(walletBalanceRequestDto));
        }else if (TB.equals(profile)) {
            return decodedDto(mpsClientTb.getWalletBalance(walletBalanceRequestDto));
        }
        return decodedDto(mpsClientLocal.getWalletBalance(walletBalanceRequestDto));
    }


    public Object getWalletBalanceDetail(FeignClientRequestDto feignClientRequestDto, String profile) {
        WalletBalanceRequestDto walletBalanceRequestDto
                = WalletBalanceRequestDto.builder()
                .custNo(feignClientRequestDto.getCustNo())
                .build();
        if (PRD.equals(profile)) {
            return decodedDto(mpsClientPrd.getWalletBalanceDetail(walletBalanceRequestDto));
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam1.getWalletBalanceDetail(walletBalanceRequestDto));
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam2.getWalletBalanceDetail(walletBalanceRequestDto));
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo1.getWalletBalanceDetail(walletBalanceRequestDto));
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo2.getWalletBalanceDetail(walletBalanceRequestDto));
        }else if (TB.equals(profile)) {
            return decodedDto(mpsClientTb.getWalletBalanceDetail(walletBalanceRequestDto));
        }
        return decodedDto(mpsClientLocal.getWalletBalanceDetail(walletBalanceRequestDto));
    }


    public Object getWithdrawableWalletBalance(FeignClientRequestDto feignClientRequestDto, String profile) {
        WithdrawalMoneyRequestDto withdrawalMoneyRequestDto =
                WithdrawalMoneyRequestDto.builder()
                        .custNo(feignClientRequestDto.getCustNo())
                        .build();
        if (PRD.equals(profile)) {
            return decodedDto(mpsClientPrd.getWithdrawableWalletBalance(withdrawalMoneyRequestDto));
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam1.getWithdrawableWalletBalance(withdrawalMoneyRequestDto));
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam2.getWithdrawableWalletBalance(withdrawalMoneyRequestDto));
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo1.getWithdrawableWalletBalance(withdrawalMoneyRequestDto));
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo2.getWithdrawableWalletBalance(withdrawalMoneyRequestDto));
        }else if (TB.equals(profile)) {
            return decodedDto(mpsClientTb.getWithdrawableWalletBalance(withdrawalMoneyRequestDto));
        }
        return decodedDto(mpsClientLocal.getWithdrawableWalletBalance(withdrawalMoneyRequestDto));

    }


    public Object approvalCharge(FeignClientRequestDto feignClientRequestDto, String profile) {
        feignClientRequestDto.createNewTrdNo();
        ChargeApprovalRequestDto chargeApprovalRequestDto =
                ChargeApprovalRequestDto.builder()
                        .custNo(feignClientRequestDto.getCustNo())
                        .mTrdNo(feignClientRequestDto.getMTrdNo())
                        .divCd(feignClientRequestDto.getDivCd())
                        .chrgMeanCd(feignClientRequestDto.getChrgMeanCd())
                        .trdAmt(getEncVal(profile, feignClientRequestDto.getTrdAmt()))
                        .blcAmt(getEncVal(profile, feignClientRequestDto.getBlcAmt()))
                        .pntVldPd(feignClientRequestDto.getPntVldPd())
                        .custBdnFeeAmt(getEncVal(profile, feignClientRequestDto.getCustBdnFeeAmt()))
                        .trdSumry(feignClientRequestDto.getTrdSumry())
                        .pktHash(makePktHash(profile, feignClientRequestDto))
                        .reqDt(feignClientRequestDto.getReqDt())
                        .reqTm(feignClientRequestDto.getReqTm())
                        .chrgTrdNo(feignClientRequestDto.getChrgTrdNo())
                        .build();
        if (PRD.equals(profile)) {
            return decodedDto(profile, mpsClientPrd.approvalCharge(chargeApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam1.approvalCharge(chargeApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam2.approvalCharge(chargeApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo1.approvalCharge(chargeApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo2.approvalCharge(chargeApprovalRequestDto), feignClientRequestDto.getDivCd());
        }else if (TB.equals(profile)) {
            return decodedDto(profile, mpsClientTb.approvalCharge(chargeApprovalRequestDto), feignClientRequestDto.getDivCd());
        }
        return decodedDto(profile, mpsClientLocal.approvalCharge(chargeApprovalRequestDto), feignClientRequestDto.getDivCd());

    }


    public Object approvalChargeCancel(FeignClientRequestDto feignClientRequestDto, String profile) {
        feignClientRequestDto.createNewTrdNo();
        ChargeApprovalCancelRequestDto chargeApprovalCancelRequestDto
                = ChargeApprovalCancelRequestDto.builder()
                .custNo(feignClientRequestDto.getCustNo())
                .mTrdNo(feignClientRequestDto.getMTrdNo())
                .orgTrdDt(feignClientRequestDto.getOrgTrdDt())
                .orgTrdNo(feignClientRequestDto.getOrgTrdNo())
                .divCd(feignClientRequestDto.getDivCd())
                .blcAmt(getEncVal(profile, feignClientRequestDto.getBlcAmt()))
                .trdSumry(feignClientRequestDto.getTrdSumry())
                .reqDt(feignClientRequestDto.getReqDt())
                .reqTm(feignClientRequestDto.getReqTm())
                .pktHash(makePktHash(profile, feignClientRequestDto))
                .build();
        if (PRD.equals(profile)) {
            return decodedDto(profile, mpsClientPrd.approvalChargeCancel(chargeApprovalCancelRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam1.approvalChargeCancel(chargeApprovalCancelRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam2.approvalChargeCancel(chargeApprovalCancelRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo1.approvalChargeCancel(chargeApprovalCancelRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo2.approvalChargeCancel(chargeApprovalCancelRequestDto), feignClientRequestDto.getDivCd());
        }else if (TB.equals(profile)) {
            return decodedDto(profile, mpsClientTb.approvalChargeCancel(chargeApprovalCancelRequestDto), feignClientRequestDto.getDivCd());
        }
        return decodedDto(profile, mpsClientLocal.approvalChargeCancel(chargeApprovalCancelRequestDto), feignClientRequestDto.getDivCd());

    }


    public Object withdrawMoney(FeignClientRequestDto feignClientRequestDto, String profile) {
        feignClientRequestDto.createNewTrdNo();
        WithdrawApprovalRequestDto withdrawApprovalRequestDto
                = WithdrawApprovalRequestDto.builder()
                .custNo(feignClientRequestDto.getCustNo())
                .mTrdNo(feignClientRequestDto.getMTrdNo())
                .divCd(feignClientRequestDto.getDivCd())
                .trdAmt(getEncVal(profile, feignClientRequestDto.getTrdAmt()))
                .mnyBlc(getEncVal(profile, feignClientRequestDto.getMnyBlc()))
                .custBdnFeeAmt(getEncVal(profile, feignClientRequestDto.getCustBdnFeeAmt()))
                .reqDt(feignClientRequestDto.getReqDt())
                .reqTm(feignClientRequestDto.getReqTm())
                .pinNo(getEncVal(profile, feignClientRequestDto.getPinNo()))
                .pktHash(makePktHash(profile, feignClientRequestDto))
                .build();
        if (PRD.equals(profile)) {
            return decodedDto(profile, mpsClientPrd.withdrawMoney(withdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam1.withdrawMoney(withdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam2.withdrawMoney(withdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo1.withdrawMoney(withdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo2.withdrawMoney(withdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        }else if (TB.equals(profile)) {
            return decodedDto(profile, mpsClientTb.withdrawMoney(withdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        }
        return decodedDto(profile, mpsClientLocal.withdrawMoney(withdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
    }


    public Object withdrawWaitMoney(FeignClientRequestDto feignClientRequestDto, String profile) {
        feignClientRequestDto.setDivCd("WW");
        feignClientRequestDto.createNewTrdNo();
        WaitMnyWithdrawApprovalRequestDto waitMnyWithdrawApprovalRequestDto =
                WaitMnyWithdrawApprovalRequestDto.builder()
                        .custNo(feignClientRequestDto.getCustNo())
                        .mTrdNo(feignClientRequestDto.getMTrdNo())
                        .divCd(feignClientRequestDto.getDivCd())
                        .waitMnyBlc(getEncVal(profile, feignClientRequestDto.getWaitMnyBlc()))
                        .reqDt(feignClientRequestDto.getReqDt())
                        .reqTm(feignClientRequestDto.getReqTm())
                        .pinNo(getEncVal(profile, feignClientRequestDto.getPinNo()))
                        .pktHash(makePktHash(profile, feignClientRequestDto))
                        .build();
        if (PRD.equals(profile)) {
            return decodedDto(profile, mpsClientPrd.withdrawWaitMoney(waitMnyWithdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam1.withdrawWaitMoney(waitMnyWithdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam2.withdrawWaitMoney(waitMnyWithdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo1.withdrawWaitMoney(waitMnyWithdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo2.withdrawWaitMoney(waitMnyWithdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        }else if (TB.equals(profile)) {
            return decodedDto(profile, mpsClientTb.withdrawWaitMoney(waitMnyWithdrawApprovalRequestDto), feignClientRequestDto.getDivCd());
        }
        return decodedDto(profile, mpsClientLocal.withdrawWaitMoney(waitMnyWithdrawApprovalRequestDto), feignClientRequestDto.getDivCd());

    }


    public Object giftMoney(FeignClientRequestDto feignClientRequestDto, String profile) {
        feignClientRequestDto.setDivCd("MG");
        feignClientRequestDto.createNewTrdNo();
        MoneyGiftRequestDto moneyGiftRequestDto =
                MoneyGiftRequestDto.builder()
                        .custNo(feignClientRequestDto.getCustNo())
                        .resCustNo(feignClientRequestDto.getResCustNo())
                        .mTrdNo(feignClientRequestDto.getMTrdNo())
                        .divCd(feignClientRequestDto.getDivCd())
                        .trdAmt(getEncVal(profile, feignClientRequestDto.getTrdAmt()))
                        .mnyBlc(getEncVal(profile, feignClientRequestDto.getMnyBlc()))
                        .reqDt(feignClientRequestDto.getReqDt())
                        .reqTm(feignClientRequestDto.getReqTm())
                        .pinNo(getEncVal(profile, feignClientRequestDto.getPinNo()))
                        .pktHash(makePktHash(profile, feignClientRequestDto))
                        .build();
        if (PRD.equals(profile)) {
            return decodedDto(profile, mpsClientPrd.giftMoney(moneyGiftRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam1.giftMoney(moneyGiftRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam2.giftMoney(moneyGiftRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo1.giftMoney(moneyGiftRequestDto), feignClientRequestDto.getDivCd());
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo2.giftMoney(moneyGiftRequestDto), feignClientRequestDto.getDivCd());
        }else if (TB.equals(profile)) {
            return decodedDto(profile, mpsClientTb.giftMoney(moneyGiftRequestDto), feignClientRequestDto.getDivCd());
        }
        return decodedDto(profile, mpsClientLocal.giftMoney(moneyGiftRequestDto), feignClientRequestDto.getDivCd());

    }


    public Object getTradeList(FeignClientRequestDto feignClientRequestDto, String profile) {
        TradeInfoListRequestDto tradeInfoListRequestDto =
                TradeInfoListRequestDto.builder()
                        .custNo(feignClientRequestDto.getCustNo())
                        .ci(getEncVal(profile, new DatabaseAESCryptoUtil().convertToEntityAttribute(feignClientRequestDto.getCi())))
                        .period(feignClientRequestDto.getPeriod())
                        .page(feignClientRequestDto.getPage())
                        .size(feignClientRequestDto.getSize())
                        .trdDivCd(feignClientRequestDto.getTrdDivCd())
                        .mTrdNo(feignClientRequestDto.getMTrdNo())
                        .trdNo(feignClientRequestDto.getTrdNo())
                        .showCnclYn(feignClientRequestDto.getShowCnclYn())
                        .blcDivCd(feignClientRequestDto.getBlcDivCd())
                        .cardTrdOnlyYn(feignClientRequestDto.getCardTrdOnlyYn())
                        .build();

        if (PRD.equals(profile)) {
            return decodedDto(mpsClientPrd.getTradeList(tradeInfoListRequestDto));
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam1.getTradeList(tradeInfoListRequestDto));
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam2.getTradeList(tradeInfoListRequestDto));
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo1.getTradeList(tradeInfoListRequestDto));
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo2.getTradeList(tradeInfoListRequestDto));
        }else if (TB.equals(profile)) {
            return decodedDto(mpsClientTb.getTradeList(tradeInfoListRequestDto));
        }
        return decodedDto(mpsClientLocal.getTradeList(tradeInfoListRequestDto));

    }


    public Object getTradeDetail(FeignClientRequestDto feignClientRequestDto, String profile) {
        TradeInfoRequestDto tradeInfoRequestDto =
                TradeInfoRequestDto.builder()
                        .custNo(feignClientRequestDto.getCustNo())
                        .ci(getEncVal(profile, new DatabaseAESCryptoUtil().convertToEntityAttribute(feignClientRequestDto.getCi())))
                        .trdDt(feignClientRequestDto.getTrdDt())
                        .trdNo(feignClientRequestDto.getTrdNo())
                        .build();
        if (PRD.equals(profile)) {
            return decodedDto(mpsClientPrd.getTradeDetail(tradeInfoRequestDto));
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam1.getTradeDetail(tradeInfoRequestDto));
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam2.getTradeDetail(tradeInfoRequestDto));
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo1.getTradeDetail(tradeInfoRequestDto));
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo2.getTradeDetail(tradeInfoRequestDto));
        }else if (TB.equals(profile)) {
            return decodedDto(mpsClientTb.getTradeDetail(tradeInfoRequestDto));
        }
        return decodedDto(mpsClientLocal.getTradeDetail(tradeInfoRequestDto));

    }


    public Object getMarketChargeList(FeignClientRequestDto feignClientRequestDto, String profile) {
        Map<String, Object> mIdParam = new HashMap<>();
        mIdParam.put("mid", feignClientRequestDto.getMid());
        if (PRD.equals(profile)) {
            return decodedDto(mpsClientPrd.getMarketChargeList(mIdParam));
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam1.getMarketChargeList(mIdParam));
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam2.getMarketChargeList(mIdParam));
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo1.getMarketChargeList(mIdParam));
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo2.getMarketChargeList(mIdParam));
        }else if (TB.equals(profile)) {
            return decodedDto(mpsClientTb.getMarketChargeList(mIdParam));
        }
        return decodedDto(mpsClientLocal.getMarketChargeList(mIdParam));
    }

    public Object getWillWithdrawalMoney(FeignClientRequestDto feignClientRequestDto, String profile) {
        WillMnyWithdrawalYnRequestDto willMnyWithdrawalYnRequestDto
                = WillMnyWithdrawalYnRequestDto.builder()
                            .custNo(feignClientRequestDto.getCustNo())
                            .build();
        if (PRD.equals(profile)) {
            return decodedDto(mpsClientPrd.getWillWithdrawalMoney(willMnyWithdrawalYnRequestDto));
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam1.getWillWithdrawalMoney(willMnyWithdrawalYnRequestDto));
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam2.getWillWithdrawalMoney(willMnyWithdrawalYnRequestDto));
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo1.getWillWithdrawalMoney(willMnyWithdrawalYnRequestDto));
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo2.getWillWithdrawalMoney(willMnyWithdrawalYnRequestDto));
        }else if (TB.equals(profile)) {
            return decodedDto(mpsClientTb.getWillWithdrawalMoney(willMnyWithdrawalYnRequestDto));
        }
        return decodedDto(mpsClientLocal.getWillWithdrawalMoney(willMnyWithdrawalYnRequestDto));

    }

    public Object getCustomerInfo(FeignClientRequestDto feignClientRequestDto, String profile) {
        String custNo = feignClientRequestDto.getCustNo();
        String custId = feignClientRequestDto.getCustId();

        CustomerRequestDto customerRequestDto = null;
        if (StringUtils.isNotBlank(custId) && StringUtils.isNotBlank(custNo)) {
            //둘다 있음
            customerRequestDto = CustomerRequestDto.builder()
                    .custId(getEncVal(profile, custId))
                    .custNo(custNo)
                    .mid(feignClientRequestDto.getMid())
                    .ci(getEncVal(profile, new DatabaseAESCryptoUtil().convertToEntityAttribute(feignClientRequestDto.getCi()))).build();
        } else if (StringUtils.isNotBlank(custId) && StringUtils.isBlank(custNo)) {
            //custId만
            customerRequestDto = CustomerRequestDto.builder()
                    .custId(getEncVal(profile, custId))
                    .mid(feignClientRequestDto.getMid())
                    .ci(getEncVal(profile, new DatabaseAESCryptoUtil().convertToEntityAttribute(feignClientRequestDto.getCi()))).build();
        } else if (StringUtils.isBlank(custId) && StringUtils.isNotBlank(custNo)) {
            //custNo만
            customerRequestDto = CustomerRequestDto.builder()
                    .custNo(custNo)
                    .mid(feignClientRequestDto.getMid())
                    .ci(getEncVal(profile, new DatabaseAESCryptoUtil().convertToEntityAttribute(feignClientRequestDto.getCi()))).build();
        }

        Map<String, Object> result = new HashMap<>();
        if (PRD.equals(profile)) {
            result = (Map<String, Object>) decodedDto(mpsClientPrd.getCustomerInfo(customerRequestDto));
        }else if (PRD_YS1.equals(profile)) {
            result = (Map<String, Object>) decodedDto(mpsClientPrdYeoksam1.getCustomerInfo(customerRequestDto));
        } else if (PRD_YS2.equals(profile)) {
            result = (Map<String, Object>) decodedDto(mpsClientPrdYeoksam2.getCustomerInfo(customerRequestDto));
        } else if (PRD_SD1.equals(profile)) {
            result = (Map<String, Object>) decodedDto(mpsClientPrdSongdo1.getCustomerInfo(customerRequestDto));
        } else if (PRD_SD2.equals(profile)) {
            result = (Map<String, Object>) decodedDto(mpsClientPrdSongdo2.getCustomerInfo(customerRequestDto));
        } else if (TB.equals(profile)) {
            result = (Map<String, Object>) decodedDto(mpsClientTb.getCustomerInfo(customerRequestDto));
        }else{
            result = (Map<String, Object>) decodedDto(mpsClientLocal.getCustomerInfo(customerRequestDto));
        }

        if(result.containsKey("custId")){
            //유효한 회원
            result.put("custId", FeignClientUtils.getDecVal(profile, String.valueOf(result.get("custId")))+"(복호화)");
            result.put("mid", FeignClientUtils.getDecVal(profile, String.valueOf(result.get("mid")))+"(복호화)");
        }
        return result;
    }



    public Object cardUse(FeignClientRequestDto feignClientRequestDto, String profile) {
        String trdDivCd = TrdDivCd.COMMON_USE.getTrdDivCd();
        feignClientRequestDto.setTrdDivCd(trdDivCd);
        String encode = null;
        try {
            if (StringUtils.isNotEmpty(feignClientRequestDto.getStorNm())) {
                encode = URLEncoder.encode(feignClientRequestDto.getStorNm(), "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            log.info("URL 인코딩 오류, {} ", feignClientRequestDto.getStorNm());
        }
        feignClientRequestDto.createNewTrdNo();
        CardUseApprovalRequestDto cardUseApprovalRequestDto =
                CardUseApprovalRequestDto.builder()
                        .custNo(feignClientRequestDto.getCustNo())
                        .mTrdNo(feignClientRequestDto.getMTrdNo())
                        .trdAmt(getEncVal(profile, feignClientRequestDto.getTrdAmt()))
                        .mnyBlc(getEncVal(profile, feignClientRequestDto.getMnyBlc()))
                        .pntBlc(getEncVal(profile, feignClientRequestDto.getPntBlc()))
                        .blcUseOrd(feignClientRequestDto.getBlcUseOrd())
                        .reqDt(feignClientRequestDto.getReqDt())
                        .reqTm(feignClientRequestDto.getReqTm())
                        .csrcIssReqYn(feignClientRequestDto.getCsrcIssReqYn())
                        .stlMId(feignClientRequestDto.getStlMId())
                        .storCd(feignClientRequestDto.getStorCd())
                        .storNm(encode)
                        .cardMngNo(feignClientRequestDto.getCardMngNo())
                        .pktHash(makePktHash(profile, feignClientRequestDto))
                        .build();

        if (PRD.equals(profile)) {
            return decodedDto(profile, mpsClientPrd.cardUse(cardUseApprovalRequestDto), trdDivCd);
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam1.cardUse(cardUseApprovalRequestDto), trdDivCd);
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdYeoksam2.cardUse(cardUseApprovalRequestDto), trdDivCd);
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo1.cardUse(cardUseApprovalRequestDto), trdDivCd);
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(profile, mpsClientPrdSongdo2.cardUse(cardUseApprovalRequestDto), trdDivCd);
        }else if (TB.equals(profile)) {
            return decodedDto(profile, mpsClientTb.cardUse(cardUseApprovalRequestDto), trdDivCd);
        }
        return decodedDto(profile, mpsClientLocal.cardUse(cardUseApprovalRequestDto), trdDivCd);
    }

    public Object getTradeUseSum(FeignClientRequestDto feignClientRequestDto, String profile) {
        TradeUseSummaryRequestDto tradeUseSummaryRequestDto = TradeUseSummaryRequestDto.builder()
                .custNo(feignClientRequestDto.getCustNo())
                .ci(getEncVal(profile, new DatabaseAESCryptoUtil().convertToEntityAttribute(feignClientRequestDto.getCi())))
                .period(feignClientRequestDto.getPeriod())
                .cardTrdOnlyYn(feignClientRequestDto.getCardTrdOnlyYn())
                .build();

        if (PRD.equals(profile)) {
            return decodedDto(mpsClientPrd.getTradeUseSum(tradeUseSummaryRequestDto));
        } else if (PRD_YS1.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam1.getTradeUseSum(tradeUseSummaryRequestDto));
        } else if (PRD_YS2.equals(profile)) {
            return decodedDto(mpsClientPrdYeoksam2.getTradeUseSum(tradeUseSummaryRequestDto));
        } else if (PRD_SD1.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo1.getTradeUseSum(tradeUseSummaryRequestDto));
        } else if (PRD_SD2.equals(profile)) {
            return decodedDto(mpsClientPrdSongdo2.getTradeUseSum(tradeUseSummaryRequestDto));
        }else if (TB.equals(profile)) {
            return decodedDto(mpsClientTb.getTradeUseSum(tradeUseSummaryRequestDto));
        }
        return decodedDto(mpsClientLocal.getTradeUseSum(tradeUseSummaryRequestDto));

    }
}
