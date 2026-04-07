package kr.co.hectofinancial.mps.api.v1.trade.service.money;

import kr.co.hectofinancial.mps.api.v1.trade.domain.RfdRcpt;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.RfdRcptInsertDto;
import kr.co.hectofinancial.mps.api.v1.trade.repository.RfdRcptRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfdRcptInsertService {

    private final RfdRcptRepository rfdRcptRepository;
    @Transactional
    public void insertRfdRcpt(RfdRcptInsertDto rfdRcptInsertDto) {
        rfdRcptRepository.save(RfdRcpt.builder()
                .rfdRcptNo(rfdRcptInsertDto.getRfdRcptNo())
                .mid(rfdRcptInsertDto.getMid())
                .svcCd(rfdRcptInsertDto.getSvcCd())
                .prdtCd(rfdRcptInsertDto.getPrdtCd())
                .rcptDt(rfdRcptInsertDto.getRcptDt())
                .rcptTm(rfdRcptInsertDto.getRcptTm())
                .orgTrdAmt(rfdRcptInsertDto.getOrgTrdAmt())
                .rfdAmt(rfdRcptInsertDto.getRfdAmt())
                .rfdDt(rfdRcptInsertDto.getRfdDt())
                .rfdSchDt(rfdRcptInsertDto.getRfdSchDt())
                .rfdAcntBankCd(rfdRcptInsertDto.getRfdAcntBankCd())
                .rfdAcntNoEnc(rfdRcptInsertDto.getRfdAcntNoEnc())
                .rfdAcntDprNm(rfdRcptInsertDto.getRfdAcntDprNm())
                .rfdAcntNoMsk(rfdRcptInsertDto.getRfdAcntNoMsk())
                .rfdAcntSumry(rfdRcptInsertDto.getRfdAcntSumry())
                .rfdStatCd(rfdRcptInsertDto.getRfdStatCd())
                .retryCnt(rfdRcptInsertDto.getRetryCnt())
                .macntSumry(rfdRcptInsertDto.getMacntSumry())
                .macntBankCd(rfdRcptInsertDto.getMacntBankCd())
                .macntNoEnc(rfdRcptInsertDto.getMacntNoEnc())
                .macntNoMsk(rfdRcptInsertDto.getMacntNoMsk())
                .rfdApprStatCd(rfdRcptInsertDto.getRfdApprStatCd())
                .rmk(rfdRcptInsertDto.getRmk())
                .trsfMid(rfdRcptInsertDto.getTrsfMid())
                .createdDate(LocalDateTime.now())
                .createdIp(ServerInfoConfig.HOST_IP)
                .createdId(ServerInfoConfig.HOST_NAME)
                .build());
    }
}
