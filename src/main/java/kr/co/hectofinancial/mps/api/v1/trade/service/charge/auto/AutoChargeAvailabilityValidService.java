package kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto;

import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustChrgMeanDto;
import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarket;
import kr.co.hectofinancial.mps.api.v1.market.repository.MpsMarketRepository;
import kr.co.hectofinancial.mps.global.constant.AutoChargeMeanCd;
import kr.co.hectofinancial.mps.global.constant.CustDivCd;
import kr.co.hectofinancial.mps.global.constant.CustStatCd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoChargeAvailabilityValidService {
    private final MpsMarketRepository mpsMarketRepository;

    public boolean isAutoChargeableMarket(String mid) {
        MpsMarket mpsMarket = mpsMarketRepository.findMpsMarketByMid(mid).get();
        String autoChrgMeanCd = mpsMarket.getAutoChrgMeanCd();

        //autoChrgMeanCd = N 일때 빼고는 자동충전 사용가능 상점
        if (autoChrgMeanCd == null || AutoChargeMeanCd.NONE.getCode().equalsIgnoreCase(autoChrgMeanCd)) {
            log.info("[isAutoChargeableMarket OUT] mid={} 자동충전 *미사용* 상점!", mid);
            return false;
        }
        log.info("[isAutoChargeableMarket OK] mid={} 자동충전 가능 상점!", mid);
        return true;
    }

    public boolean isAutoChargeableCust(CustChrgMeanDto custChrgMeanDto) {
        //고객 자동충전 사용여부 (AUTO_CHRG_YN 컬럼)
        if (!custChrgMeanDto.isAutoChargeUse()) {
            log.info("[isAutoChargeableCust OUT] custNo={} 고객이 자동충전 *미사용* (N)", custChrgMeanDto.getMpsCustNo());
            return false;
        }
        //고객 자동충전 수단 (계좌, 카드) 확인 (AUTO_CHRG_ACCNT 컬럼)
        if (custChrgMeanDto.getAutoChargeMethods().isEmpty()) {
            log.info("[isAutoChargeableCust OUT] custNo={} 고객 자동충전 수단 *미등록* (N)", custChrgMeanDto.getMpsCustNo());
            return false;
        }
        //자동충전 가능
        log.info("[isAutoChargeableCust OK] custNo={} 고객 자동충전 사용 가능! ", custChrgMeanDto.getMpsCustNo());
        return true;
    }

    public boolean isNamedCust(CustomerDto customerDto) {
        //고객 기명/무기명 여부 (자동충전은 기명만 사용가능)
        return CustDivCd.NAMED.getCustDivCd().equals(customerDto.getCustDivCd());
    }
}
