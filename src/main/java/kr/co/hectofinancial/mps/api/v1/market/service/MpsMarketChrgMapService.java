package kr.co.hectofinancial.mps.api.v1.market.service;

import kr.co.hectofinancial.mps.api.v1.market.dto.MarketChargeMapResponseDto;
import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarketChrgMap;
import kr.co.hectofinancial.mps.api.v1.market.repository.MpsMarketChrgMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MpsMarketChrgMapService {

    private final MpsMarketChrgMapRepository mpsMarketChrgMapRepository;
    public List<MarketChargeMapResponseDto> getMaketChargeMapList(String mid){

        List<Map<String, Object>> chargeList = mpsMarketChrgMapRepository.findAllByMid(mid);
        return chargeList.stream().map(MarketChargeMapResponseDto::of).collect(Collectors.toList());
    }
}
