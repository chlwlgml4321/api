package kr.co.hectofinancial.mps.api.v1.giftcard.single.service;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.constants.GcStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.repository.GiftCardIssueRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardReIssueServiceSupport {

    private final GiftCardIssueRepository issueRepository;
    private final GiftCardCommonService giftCardCommonService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public GiftCardIssue getGiftCardIssue(String gcNoEnc) {
        try {
            return issueRepository.findByGcNoEnc(gcNoEnc);
        } catch (CannotAcquireLockException cale) { // 동시성 제어
            log.error("Gift card reissue is in progress. msg={}", cale.getMessage(), cale);
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
        }
    }

    @Transactional
    public String generateGiftCard(GiftCardIssue bfGcIssue, LocalDateTime createDate) {
        String gcNo = giftCardCommonService.generateGiftCardNo(bfGcIssue.getUseMid());
        String gcNoEnc = giftCardCommonService.encrypt(gcNo);
        String gcNoMsk = giftCardCommonService.maskingGiftCardNo(gcNo);

        GiftCardIssue issue = GiftCardIssue.builder()
                .gcNoEnc(gcNoEnc)
                .issDt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .gcNoMsk(gcNoMsk)
                .issAmt(bfGcIssue.getIssAmt())
                .blc(bfGcIssue.getIssAmt())
                .vldPd(bfGcIssue.getVldPd())
                .useMid(bfGcIssue.getUseMid())
                .gcStatCd(GcStatCd.ISSUE.getCode())
                .bfGcNoEnc(bfGcIssue.getGcNoEnc())
                .bfIssDt(bfGcIssue.getIssDt())
                .createdDate(createDate)
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();

        issueRepository.save(issue);

        // for error logging
        entityManager.flush();

        return gcNo;
    }
}
