package kr.co.hectofinancial.mps.api.v1.giftcard.single.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.DstbPrdtCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardTrade;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardTradeFail;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardUse;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardUseVo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.repository.GiftCardIssueRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.repository.GiftCardTradeFailRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.repository.GiftCardTradeRepository;
import kr.co.hectofinancial.mps.api.v1.market.domain.MarketAddInfo;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.MpsApiCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import kr.co.hectofinancial.mps.global.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardUseServiceSupport {

    private final GiftCardIssueRepository issueRepository;
    private final GiftCardTradeFailRepository tradeFailRepository;
    private final GiftCardTradeRepository tradeRepository;
    private final JsonUtil jsonUtil;
    private final GiftCardCommonService giftCardCommonService;

    @PersistenceContext
    private EntityManager entityManager;

    private final JdbcTemplate jdbcTemplate;

    public GiftCardUseVo validateAndConvertRequestDto(GiftCardUseRequestDto dto) {
        List<String> gcNoList;
        try {
            gcNoList = jsonUtil.fromJsonArray(dto.getUseGcList(), new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Json parsing error: {}", e.getMessage(), e);
            throw new RequestValidationException(ErrorCode.JSON_FORMAT_ERROR);
        }

        // 상품권 총 수량 확인
        int maxUseCnt = 50;
        if (gcNoList.size() > maxUseCnt) {
            log.info("상품권 최대 사용 수량을 초과했습니다. req={}, max={}", gcNoList.size(), maxUseCnt);
            throw new RequestValidationException(ErrorCode.GIFT_CARD_USE_LIMIT_EXCEEDED);
        }

        // 상품권 중복 사용 확인
        Set<String> reqGcNoList = new HashSet<>(gcNoList);
        if (reqGcNoList.size() != gcNoList.size()) {
            log.info("Duplicate giftcard usage is not allowed. req={}, max={}", gcNoList.size(), maxUseCnt);
            throw new RequestValidationException(ErrorCode.GIFT_CARD_USE_NOT_ALLOW_DUPLICATE);
        }

        // 서버시간은 동일하게
        LocalDateTime now = DateTimeUtil.getCurrentDateTime();
        String trdDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String trdTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);

        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(dto.getTrdDt()) && StringUtils.isNotEmpty(dto.getTrdTm())) {
            mReqDtm = dto.getTrdDt() + dto.getTrdTm(); // 요청정보가 있으면 해당 시간으로 저장
        }

        // 상품권 번호 암호화
        List<String> gcNoEncList = new ArrayList<>();
        for (String gcNo : gcNoList) {
            String gcNoEnc = giftCardCommonService.encrypt(gcNo);
            gcNoEncList.add(gcNoEnc);
        }

        String stlMid = dto.getStlMid();
        if (StringUtils.isEmpty(stlMid)) {
            stlMid = dto.getUseMid();
        }

        String trdNo = giftCardCommonService.generateTradeNo();

        // 상품권 목록 암호화 (API 응답용)
        Optional<MarketAddInfo> optionalMarketAddInfo = giftCardCommonService.getMarketAddInfoByMId(dto.getUseMid());
        if (!optionalMarketAddInfo.isPresent()) {
            throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
        }

        MarketAddInfoDto mIdInfo = MarketAddInfoDto.of(optionalMarketAddInfo.get());
        String encUseGcList;
        try {
            encUseGcList = CipherUtil.encrypt(dto.getUseGcList(), mIdInfo.getEncKey());
        } catch (Exception ex) {
            throw new RequestValidationException(ErrorCode.UNDEFINED_SERVER_ERROR_CODE);
        }

        return GiftCardUseVo.builder()
                .mTrdNo(dto.getMTrdNo())
                .reqDtm(mReqDtm)
                .encUseGcList(encUseGcList)
                .gcNoEncList(gcNoEncList)
                .svcCd(MpsApiCd.SVC_CD)
                .prdtCd(DstbPrdtCd.GIFTCARD_USE.getPrdtCd())
                .trdNo(trdNo)
                .trdDt(trdDt) // 서버시간
                .trdTm(trdTm) // 서버시간
                .useMid(dto.getUseMid())
                .stlMid(stlMid)
                .trdSumry(dto.getTrdSumry())
                .mResrvField1(dto.getMResrvField1())
                .mResrvField2(dto.getMResrvField2())
                .mResrvField3(dto.getMResrvField3())
                .createDate(LocalDateTime.now()) // 서버시간
                .build();
    }

    @Transactional(readOnly = true)
    public List<GiftCardIssue> getGiftCardList(List<String> gcNoEncList) {
        try {
            return issueRepository.findByGcNoEncIn(gcNoEncList);
        } catch (CannotAcquireLockException cale) { // 동시성 제어
            log.error("Gift card use is in progress. msg={}", cale.getMessage(), cale);
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertGiftCardTradeFail(GiftCardUseVo useVo, ErrorCode errorCode) {
        GiftCardTradeFail tradeFail = GiftCardTradeFail.builder()
                .trdNo(useVo.getTrdNo())
                .failDt(useVo.getTrdDt()) // 서버시간
                .failTm(useVo.getTrdTm()) // 서버시간
                .svcCd(useVo.getSvcCd())
                .prdtCd(useVo.getPrdtCd())
                .useMid(useVo.getUseMid())
                .stlMid(useVo.getStlMid())
                .cnclYn("N")
                .amtSign(-1)
                .trdAmt(useVo.getTrdAmt())
                .mReqDtm(useVo.getReqDtm()) // 요청일시
                .mTrdNo(useVo.getMTrdNo())
                .errCd(errorCode.getErrorCode())
                .errMsg(errorCode.getErrorMessage())
                .gcList(jsonUtil.toJson(useVo.getGcNoEncList()))
                .trdSumry(useVo.getTrdSumry())
                .mResrvField1(useVo.getMResrvField1())
                .mResrvField2(useVo.getMResrvField2())
                .mResrvField3(useVo.getMResrvField3())
                .createdDate(useVo.getCreateDate()) // 서버시간
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .bndlGcNoEnc(useVo.getBndlPinNoEnc())
                .build();
        tradeFailRepository.save(tradeFail);

        // for error logging
        entityManager.flush();
    }

    @Transactional
    public void insertGiftCardTradeSuccess(GiftCardUseVo useVo) {
        // 거래내역 저장
        GiftCardTrade trade = GiftCardTrade.builder()
                .trdNo(useVo.getTrdNo())
                .trdDt(useVo.getTrdDt())
                .trdTm(useVo.getTrdTm())
                .svcCd(useVo.getSvcCd())
                .prdtCd(useVo.getPrdtCd())
                .amtSign(-1)
                .cnclYn("N")
                .useMid(useVo.getUseMid())
                .stlMid(useVo.getStlMid())
                .trdAmt(useVo.getTrdAmt())
                .mReqDtm(useVo.getReqDtm())
                .mTrdNo(useVo.getMTrdNo())
                .gcList(jsonUtil.toJson(useVo.getGcNoEncList()))
                .bndlPinNoEnc(useVo.getBndlPinNoEnc())
                .trdSumry(useVo.getTrdSumry())
                .mResrvField1(useVo.getMResrvField1())
                .mResrvField2(useVo.getMResrvField2())
                .mResrvField3(useVo.getMResrvField3())
                .createdDate(useVo.getCreateDate())
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdIp(ServerInfoConfig.HOST_IP)
                .build();

        tradeRepository.save(trade);

        // for error logging
        entityManager.flush();
    }

    @Transactional
    public void insertGiftCardUseHistory(GiftCardUseVo useVo) {
        LocalDateTime createDate = useVo.getCreateDate();

        // 상품권 사용 등록
        List<GiftCardUse> gcUseList = new ArrayList<>();
        for (GiftCardIssue gcInfo : useVo.getGcIssueList()) {
            GiftCardUse gcUse = GiftCardUse.builder()
                    .gcNoEnc(gcInfo.getGcNoEnc())
                    .issDt(gcInfo.getIssDt())
                    .useDate(createDate) // 서버시간
                    .amtSign(-1)
                    .cnclYn("N")
                    .useAmt(gcInfo.getIssAmt())
                    .blc(0)
                    .trdNo(useVo.getTrdNo()) // 서버시간
                    .trdDt(useVo.getTrdDt()) // 서버시간
                    .createdDate(createDate) // 서버시간
                    .createdId(ServerInfoConfig.HOST_NAME)
                    .createdIp(ServerInfoConfig.HOST_IP)
                    .build();
            gcUseList.add(gcUse);
        }

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
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                GiftCardUse gcUse = gcUseList.get(i);
                ps.setString(1, gcUse.getGcNoEnc());
                ps.setString(2, gcUse.getIssDt());
                ps.setObject(3, gcUse.getUseDate());
                ps.setInt(4, gcUse.getAmtSign());
                ps.setString(5, gcUse.getCnclYn());
                ps.setLong(6, gcUse.getUseAmt());
                ps.setLong(7, gcUse.getBlc());
                ps.setString(8, gcUse.getTrdNo());
                ps.setString(9, gcUse.getTrdDt());
                ps.setObject(10, gcUse.getCreatedDate());
                ps.setString(11, gcUse.getCreatedId());
                ps.setString(12, gcUse.getCreatedIp());
            }

            @Override
            public int getBatchSize() {
                return gcUseList.size();
            }
        });
    }
}
