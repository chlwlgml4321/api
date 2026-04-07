package kr.co.hectofinancial.mps.api.v1.trade.service.money;

import kr.co.hectofinancial.mps.api.v1.trade.domain.RfdRsltCnf;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.RfdRsltCnfInsertDto;
import kr.co.hectofinancial.mps.api.v1.trade.repository.RfdRsltCnfRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfdRsltCnfInsertService {

    private final RfdRsltCnfRepository rfdRsltCnfRepository;
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertRfdRsltCnf(RfdRsltCnfInsertDto rfdRsltCnfInsertDto) {
        rfdRsltCnfRepository.save(RfdRsltCnf.builder()
                .trdNo(rfdRsltCnfInsertDto.getTrdNo())
                .trdDt(rfdRsltCnfInsertDto.getTrdDt())
                .trdTm(rfdRsltCnfInsertDto.getTrdTm())
                .rfdTrdNo(rfdRsltCnfInsertDto.getRfdTrdNo())
                .mid(rfdRsltCnfInsertDto.getMid())
                .mpsCustNo(rfdRsltCnfInsertDto.getMpsCustNo())
                .trdAmt(rfdRsltCnfInsertDto.getTrdAmt())
                .rsltCnfStatCd(rfdRsltCnfInsertDto.getRsltCnfStatCd())
                .rsltCnfCnt(rfdRsltCnfInsertDto.getRsltCnfCnt())
                .lastRsltCnfDate(LocalDateTime.now())
                .reprocRfdTrdNo(rfdRsltCnfInsertDto.getReprocRfdTrdNo())
                .reprocRfdDate(rfdRsltCnfInsertDto.getReprocRfdDate())
                .reprocRfdStatCd(rfdRsltCnfInsertDto.getReprocRfdStatCd())
                .reprocRfdRsltCd(rfdRsltCnfInsertDto.getReprocRfdRsltCd())
                .rmk(rfdRsltCnfInsertDto.getRmk())
                .rfdAcntBankCd(rfdRsltCnfInsertDto.getRfdAcntBankCd())
                .rfdAcntNoEnc(rfdRsltCnfInsertDto.getRfdAcntNoEnc())
                .rfdAcntNoMsk(rfdRsltCnfInsertDto.getRfdAcntNoMsk())
                .rmtDivCd(rfdRsltCnfInsertDto.getRmtDivCd())
                .createdDate(LocalDateTime.now())
                .createdIp(ServerInfoConfig.HOST_IP)
                .createdId(ServerInfoConfig.HOST_NAME)
                .build());
    }
}
