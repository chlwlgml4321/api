package kr.co.hectofinancial.mps.api.v1.cpn.service;

import kr.co.hectofinancial.mps.api.v1.cpn.domain.Cpn;
import kr.co.hectofinancial.mps.api.v1.cpn.domain.CpnM;
import kr.co.hectofinancial.mps.api.v1.cpn.repository.CpnMRepository;
import kr.co.hectofinancial.mps.api.v1.cpn.repository.CpnRepository;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CpnService {

    private final CpnMRepository cpnMRepository;
    private final CpnRepository cpnRepository;

    public String getCpnId(String mid) {
        CpnM cpnM = cpnMRepository.findByMid(mid).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));
        return cpnM.getCpnId();
    }

    public Cpn getCpn(String cpnId) {
        return cpnRepository.findByCpnId(cpnId).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));
    }
}
