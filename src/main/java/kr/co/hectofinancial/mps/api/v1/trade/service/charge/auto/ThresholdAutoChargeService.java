package kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto;

import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.CustomerWalletResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.service.wallet.CustBalanceService;
import kr.co.hectofinancial.mps.global.constant.AutoChargeType;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustAutoChargeInfo;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustChrgMeanDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.PgPayRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.PgPayResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MpsMarketDto;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class ThresholdAutoChargeService {

    private final CustChrgMeanService custChrgMeanService;
    private final AutoChargeAvailabilityValidService autoChargeAvailabilityValidService;
    private final CommonService commonService;
    private final CustBalanceService custBalanceService;
    private final PgPayService pgPayService;

    /**
     * 기준금액 자동충전 후 머니잔액 반환
     */
    @Async(value = "async")
    public void thresholdAutoCharge(String mnyBlc, String mTrdNo,
                                    CustChrgMeanDto custChrgMeanDto,
                                    CustomerDto customerDto,
                                    MpsMarketDto marketDto,
                                    MarketAddInfoDto marketAddInfoDto) {

        String custNo = customerDto.getMpsCustNo();
        long postUseMnyBlc = Long.parseLong(mnyBlc);
        long maxMnyBlc = customerDto.getChrgLmtAmlt();

        log.info("[ThresholdAutoCharge] 기준금액 자동충전 검증 시작! custNo={} mTrdNo={}", custNo, mTrdNo);

        if(custChrgMeanDto == null) custChrgMeanDto = custChrgMeanService.getCustChrgMeanForAutoChrg(custNo);

        //기준금액 자동충전 고객인지 확인
        if (!autoChargeAvailabilityValidService.isAutoChargeableCust(custChrgMeanDto) || !custChrgMeanDto.isThresholdEnabled()) {
            return;
        }
        //기준금액 계산
        long reqMnyAmt = getThresholdChargeAmount(postUseMnyBlc, maxMnyBlc, custChrgMeanDto);
        if (reqMnyAmt <= 0) {
            return;
        }
        log.info("[useWithAutoCharge] 기준금액 자동충전 대상! custNo={} mTrdNo={} reqAmt={}", custNo, mTrdNo, reqMnyAmt);

        if (marketDto == null) marketDto = commonService.getMpsMarketInfo(customerDto.getMid());
        if (marketAddInfoDto == null) marketAddInfoDto = commonService.getMarketAddInfoByMId(customerDto.getMid());

        String mchtCustId = "C".equals(marketDto.getPinVrifyTypeCd()) ? customerDto.getMCustId() : customerDto.getMpsCustNo();
        CustomDateTimeUtil curDt = new CustomDateTimeUtil();

        PgPayRequestDto pgPayRequestDto = PgPayRequestDto.builder()
                .mTrdNo(mTrdNo)
                .reqAmt(reqMnyAmt)
                .encKey(marketAddInfoDto.getEncKey())
                .pktHashKey(marketAddInfoDto.getPktHashKey())
                .mid(customerDto.getMid())
                .mpsCustNo(custNo)
                .mCustId(customerDto.getMCustId())
                .mchtCustId(mchtCustId)
                .blcAmt(postUseMnyBlc)
                .autoChargeType(AutoChargeType.THRESHOLD)
                .reqDt(curDt.getDate())
                .reqTm(curDt.getTime())
                .build();
        if (StringUtils.isNotBlank(CommonUtil.nullTrim(marketDto.getWdTrdSumry()))) {
            //고객 통장 적요값 상점이 설정한 값으로 변경
            pgPayRequestDto.setCustTrdSumry(marketDto.getWdTrdSumry());
        }
        log.info("[ThresholdAutoCharge] 기준금액 자동충전 시작! custNo={} mTrdNo={}", custNo, mTrdNo);

        PgPayResponseDto pgPayResponseDto = pgPayService.payAndChargeMoney(customerDto, pgPayRequestDto, custChrgMeanDto.getAutoChargeMethods(), retryHandler(custNo, maxMnyBlc, custChrgMeanDto));

        String resultCode = pgPayResponseDto.getResultCode();

        if (!"0000".equals(resultCode)) {
            log.info("[ThresholdAutoCharge FAIL] 기준금액 자동충전 실패!! custNo={} mTrdNo={}", custNo, mTrdNo);
        }else{
            log.info("[ThresholdAutoCharge SUCCESS] 기준금액 자동충전 성공! custNo={} mTrdNo={}", custNo, mTrdNo);
        }
    }

    private Supplier<Map<String, Long>> retryHandler(String custNo, long maxMnyBlc, CustChrgMeanDto custChrgMeanDto) {
        return () -> {
            Map<String, Long> mnyMap = new HashMap<>();

            CustomerWalletResponseDto custBalance = custBalanceService.getBalanceByCustomer(custNo, maxMnyBlc);
            long realMnyBlc = custBalance.getMnyBlc();
            long realReqMnyAmt = getThresholdChargeAmount(realMnyBlc, maxMnyBlc, custChrgMeanDto);
            mnyMap.put("mnyBlc", realMnyBlc);
            mnyMap.put("reqMnyAmt", realReqMnyAmt);
            return mnyMap;

        };
    }

    private long getThresholdChargeAmount(long mnyBlc, long maxMnyBlc, CustChrgMeanDto custChrgMeanDto) {
        CustAutoChargeInfo thresholdInfo = custChrgMeanDto.getThresholdInfo();
        long trgAmt = thresholdInfo.getTriggerAmountAsLong();   //기준잔액
        long chrgAmt = thresholdInfo.getChargeAmountAsLong();   //충전금액
        if (chrgAmt < trgAmt || chrgAmt <= 0 || trgAmt <= 0) {
            log.info("[getThresholdChargeAmount FALSE] 기준금액이 충전금액보다 큼, 설정오류! 기준금액={} 충전금액={}", trgAmt, chrgAmt);
            MonitAgent.sendMonitAgent(ErrorCode.AUTO_CHARGE_SETTING_ERROR.getErrorCode(), ErrorCode.AUTO_CHARGE_SETTING_ERROR.getErrorMessage() + " :custNo=" + custChrgMeanDto.getMpsCustNo());
            return 0L;
        }
        //잔액이 기준금액 초과
        if (mnyBlc > trgAmt) {
            log.info("[getThresholdChargeAmount FALSE] 기준금액 충분으로 자동충전 미대상! 머니잔액={} 기준금액={} ", mnyBlc, trgAmt);
            return 0L;
        }
        //이미 머니 잔액이 보유한도 만큼 존재
        if (maxMnyBlc <= mnyBlc) {
            log.info("[getThresholdChargeAmount FALSE] 잔액이 이미 보유한도만큼 존재하므로 자동충전 불가! 머니잔액={} 한도금액={} ", mnyBlc, maxMnyBlc);
            return 0L;
        }
        //충전 후 잔액이 머니 보유한도 초과하는지 검증 2 (기준금액 충전 후 잔액이 보유한도 넘는지 보기)
        long afterBalance = mnyBlc + chrgAmt; //충전 후 잔액
        if (afterBalance > maxMnyBlc) {
           long allowed = maxMnyBlc - mnyBlc; //한도까지 남은 충전금액
            log.info("[getThresholdChargeAmount TRUE] 기준금액 자동충전 대상! 보유한도 초과로 충전금액 조정! 기준금액={} 설정충전금액={} 실제충전금액={}", trgAmt, chrgAmt, allowed);
            return allowed;
        }
        log.info("[getThresholdChargeAmount TRUE] 기준금액 자동충전 대상! 기준금액={} 설정충전금액={} 실제충전금액={}", trgAmt, chrgAmt, chrgAmt);
        return chrgAmt;
    }
}
