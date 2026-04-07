package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.service.use;

import com.fasterxml.jackson.core.type.TypeReference;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.DstbPrdtCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcBndlConstants;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.GcBndlStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundlePin;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.GiftCardBundleUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardUseVo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.repository.GiftCardIssueRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.service.GiftCardCommonService;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.service.GiftCardUseServiceSupport;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.MpsApiCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import kr.co.hectofinancial.mps.global.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class GiftCardBundleUseServiceSupport {

    private final GiftCardCommonService giftCardCommonService;
    private final GiftCardUseServiceSupport useServiceSupport;
    private final CommonService commonService;
    private final JdbcTemplate jdbcTemplate;
    private final JsonUtil jsonUtil;
    private final GiftCardIssueRepository issueRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public GiftCardUseVo validateAndConvertRequestDto(GiftCardBundleUseRequestDto dto) {
        /* 상품권 거래내역 번호 생성 */
        String trdNo = giftCardCommonService.generateTradeNo();

        /* 정산상점 아이디 셋팅 */
        String stlMid = dto.getStlMid();

        // 서버시간은 동일하게
        LocalDateTime now = DateTimeUtil.getCurrentDateTime();
        String trdDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String trdTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);

        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(dto.getTrdDt()) && StringUtils.isNotEmpty(dto.getTrdTm())) {
            mReqDtm = dto.getTrdDt() + dto.getTrdTm(); // 요청정보가 있으면 해당 시간으로 저장
        }

        /* 정산 상점 아이디 검증 */
        commonService.checkValidStlMid(dto.getStlMid(), dto.getUseMid(), DstbPrdtCd.GIFTCARD_USE.getPrdtCd());

        /* 묶음 상품권 번호 암호화 */
        String bndlPinNoEnc = giftCardCommonService.encrypt(dto.getGcBndlNo());

        /* 거래내역 또는 실패내역 저장 데이터 셋팅 */
        return GiftCardUseVo.builder()
                .mTrdNo(dto.getMTrdNo())
                .reqDtm(mReqDtm)
                .bndlPinNoEnc(bndlPinNoEnc)
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.GIFTCARD_USE.getPrdtCd())
                .trdNo(trdNo)
                .trdDt(trdDt) // 서버시간
                .trdTm(trdTm) // 서버시간
                .trdAmt(Long.parseLong(dto.getTrdAmt()))
                .useMid(dto.getUseMid())
                .stlMid(stlMid)
                .trdSumry(dto.getTrdSumry())
                .mResrvField1(dto.getMResrvField1())
                .mResrvField2(dto.getMResrvField2())
                .mResrvField3(dto.getMResrvField3())
                .createDate(now) // 서버시간
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertGiftCardTradeFail(GiftCardUseVo useVo, ErrorCode errorCode) {
        useServiceSupport.insertGiftCardTradeFail(useVo, errorCode);
    }

    @Transactional
    public void insertGiftCardTradeSuccess(GiftCardUseVo useVo) {
        useServiceSupport.insertGiftCardTradeSuccess(useVo);
    }

    @Transactional
    public void insertGiftCardUseHistory(
            String trdNo,
            String trdDt,
            String bndlPinNoEnc,
            String bndlPinIssDt
    ) {
        String sql = "INSERT INTO MPS.PM_MPS_GC_USE (" +
                "GC_NO_ENC, " +
                "ISS_DT, " +
                "USE_DATE, " +
                "AMT_SIGN, " +
                "CNCL_YN, " +
                "USE_AMT, " +
                "BLC, " +
                "TRD_NO, " +
                "TRD_DT, " +
                "INST_DATE, " +
                "INST_ID, " +
                "INST_IP" +
                ") " +
                "SELECT GC_NO_ENC " +
                ", ISS_DT " +
                ", SYSDATE AS USE_DATE " +
                ", -1 AS AMT_SIGN " +
                ", 'N' AS CNCL_YN " +
                ", ISS_AMT AS USE_AMT " +
                ", 0 AS BLC " +
                ", ? " +
                ", ? " +
                ", SYSDATE AS INST_DATE" +
                ", ? " +
                ", ? " +
                "FROM MPS.PM_MPS_GC_ISS " +
                "WHERE BNDL_PIN_NO_ENC = ? " +
                "  AND BNDL_PIN_ISS_DT = ? "
                ;

        jdbcTemplate.update(
                sql,
                trdNo,
                trdDt,
                ServerInfoConfig.HOST_NAME,
                ServerInfoConfig.HOST_IP,
                bndlPinNoEnc,
                bndlPinIssDt
        );
    }

    @Transactional
    public void updateGiftCardBundleUse(GiftCardBundlePin gcBndlPin) {
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
                    gcBndlPin.setGcStatCd(GcBndlStatCd.USED.getCode());
                }
                /* count == 0 이면 아직 배치에서 미발행. 배치에서 상태 업데이트  */
            } else { /* 1000장 이하이면 서버에서 상태 업데이트 */
                gcBndlPin.setGcStatCd(GcBndlStatCd.USED.getCode());
            }
        } catch (Exception ex) {
            log.error("Parse error gcStatCd: {}", ex.getMessage(), ex);
        } finally {
            gcBndlPin.setPinStatCd(GcBndlStatCd.USED.getCode());
            gcBndlPin.setModifiedDate(LocalDateTime.now());
            gcBndlPin.setModifiedId(ServerInfoConfig.HOST_NAME);
            gcBndlPin.setModifiedIp(ServerInfoConfig.HOST_IP);

            // for error logging
            entityManager.flush();
        }
    }
}
