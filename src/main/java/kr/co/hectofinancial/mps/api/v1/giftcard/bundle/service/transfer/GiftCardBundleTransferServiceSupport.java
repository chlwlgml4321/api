package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.transfer;

import com.fasterxml.jackson.core.type.TypeReference;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.*;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundlePin;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTrade;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTradeFail;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.transfer.GiftCardBundleTransferRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardBundleDistributorRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardDistributorTradeFailRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository.GiftCardDistributorTradeRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.repository.GiftCardIssueRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.MpsApiCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardBundleTransferServiceSupport {

    private final GiftCardDistributorTradeRepository distributorTradeRepository;
    private final GiftCardDistributorTradeFailRepository distributorTradeFailRepository;
    private final GiftCardBundleDistributorRepository distributorRepository;
    private final GiftCardIssueRepository issueRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final JsonUtil jsonUtil;

    @Transactional
    public void transferGiftCardBundle(GiftCardBundlePin gcBndlPin) {
        try {
            List<GiftCardIssueInfo> gcIssList = jsonUtil.fromJsonArray(gcBndlPin.getBndlReqInfo(), new TypeReference<List<GiftCardIssueInfo>>() {});

            long totCnt = 0;
            for (GiftCardIssueInfo gcIss : gcIssList) {
                totCnt += Long.parseLong(gcIss.getGcQty());
            }

            /* 발행건수 확인 */
            if (totCnt > GcBndlConstants.maxIssCntOnServer) { /* 배치에서 발행하는 경우 */
                /* PM_MPS_GC_ISS에 상품권 발행되었는지 확인하는 쿼리 */
                int count = issueRepository.countByIssDtAndBndlPinNoEncAndBndlPinIssDt(
                        gcBndlPin.getIssDt(), // 파티셔닝 테이블 조회용
                        gcBndlPin.getBndlPinNoEnc(),
                        gcBndlPin.getIssDt()
                );

                if (count > 0) { /* 배치에서 이미 발행완료. 서버에서 상태 업데이트  */
                    gcBndlPin.setGcStatCd(GcBndlStatCd.TRANSFER.getCode());
                }
                /* count == 0 이면 아직 배치에서 미발행. 배치에서 상태 업데이트  */
            } else { /* 1000장 이하이면 서버에서 상태 업데이트 */
                gcBndlPin.setGcStatCd(GcBndlStatCd.TRANSFER.getCode());
            }
        } catch (Exception ex) {
            log.error("Parse error gcStatCd: {}", ex.getMessage(), ex);
        } finally {
            gcBndlPin.setPinStatCd(GcBndlStatCd.TRANSFER.getCode());
            gcBndlPin.setModifiedDate(LocalDateTime.now());
            gcBndlPin.setModifiedId(ServerInfoConfig.HOST_NAME);
            gcBndlPin.setModifiedIp(ServerInfoConfig.HOST_IP);

            entityManager.flush();
        }
    }

    @Transactional
    public long insertGiftCardDistributorBalance(String gcDstbNo, long bndlAmt) {
        GiftCardBundleDistributor distributor = new GiftCardBundleDistributor();
        distributor.setGcDstbNo(gcDstbNo);
        distributor.setDstbBlc(bndlAmt);
        distributor.setCreatedDate(LocalDateTime.now());
        distributor.setCreatedId(ServerInfoConfig.HOST_NAME);
        distributor.setCreatedIp(ServerInfoConfig.HOST_IP);

        distributorRepository.save(distributor);

        return bndlAmt;
    }

    @Transactional
    public long updateGiftCardDistributorBalance(GiftCardBundleDistributor distributor, long bndlAmt) {
        long dstbBlc = distributor.getDstbBlc() + bndlAmt;

        distributor.setDstbBlc(dstbBlc);
        distributor.setModifiedDate(LocalDateTime.now());
        distributor.setModifiedId(ServerInfoConfig.HOST_NAME);
        distributor.setModifiedIp(ServerInfoConfig.HOST_IP);

        return dstbBlc;
    }

    @Transactional
    public void insertDstbTrade(
            String dstbTrdNo,
            String trdDt,
            String trdTm,
            String mReqDtm,
            String bndlPinNoEnc,
            GiftCardBundleTransferRequestDto dto
    ) {
        /* 유통 거래 이력 저장 */
        GiftCardDistributorTrade trade = GiftCardDistributorTrade.builder()
                .dstbTrdNo(dstbTrdNo)
                .trdDt(trdDt)
                .trdTm(trdTm)
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.CHARGE.getPrdtCd())
                .cnclYn("N")
                .mId(dto.getUseMid())
                .mTrdNo(dto.getMTrdNo())
                .mReqDtm(mReqDtm)
                .chrgMeanCd(GcDstbChrgMeanCd.TRANSFER.getCode())
                .trdDivCd(GcDstbTrdDivCd.DISTRIBUTOR_CHARGE.getCode())
                .gcDstbNo(dto.getGcDstbNo())
                .bndlPinNoEnc(bndlPinNoEnc)
                .amtSign(1)
                .trgAmt(Long.parseLong(dto.getTrdAmt()))
                .createdDate(LocalDateTime.now())
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();
        distributorTradeRepository.save(trade);

        // for error logging
        entityManager.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertDstbTradeFail(
            String dstbTrdNo,
            String failDt,
            String failTm,
            String mReqDtm,
            String bndlPinNoEnc,
            GiftCardBundleTransferRequestDto dto,
            ErrorCode errorCode
    ) {
        GiftCardDistributorTradeFail tradeFail = GiftCardDistributorTradeFail.builder()
                .dstbTrdNo(dstbTrdNo)
                .failDt(failDt)
                .failTm(failTm)
                .mId(dto.getUseMid())
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.CHARGE.getPrdtCd())
                .cnclYn("N")
                .mTrdNo(dto.getMTrdNo())
                .mReqDtm(mReqDtm)
                .trdDivCd(GcDstbTrdDivCd.DISTRIBUTOR_CHARGE.getCode())
                .amtSign(1)
                .trgAmt(Long.parseLong(dto.getTrdAmt()))
                .chrgMeanCd(GcDstbChrgMeanCd.TRANSFER.getCode())
                .bndlPinNoEnc(bndlPinNoEnc)
                .gcDstbNo(dto.getGcDstbNo())
                .errCd(errorCode.getErrorCode())
                .errMsg(errorCode.getErrorMessage())
                .createdDate(LocalDateTime.now())
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();

        distributorTradeFailRepository.save(tradeFail);
        entityManager.flush();
    }
}