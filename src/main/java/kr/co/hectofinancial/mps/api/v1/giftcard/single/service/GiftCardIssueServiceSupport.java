package kr.co.hectofinancial.mps.api.v1.giftcard.single.service;

import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountRequestDto;
import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountResponseDto;
import kr.co.hectofinancial.mps.api.v1.authentication.service.AuthenticationService;
import kr.co.hectofinancial.mps.api.v1.authentication.service.BillKeyService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.constants.GcStatCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.constants.GiftCardAmountType;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueVo;
import kr.co.hectofinancial.mps.api.v1.market.domain.MarketAddInfo;
import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarket;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.market.repository.MpsMarketRepository;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeFailInsertDto;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Use.UseIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Use.UseOut;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.api.v1.trade.service.TradeFailService;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.MpsApiCd;
import kr.co.hectofinancial.mps.global.constant.MpsPrdtCd;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import kr.co.hectofinancial.mps.global.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCardIssueServiceSupport {

    private final MpsMarketRepository mpsMarketRepository;
    private final BillKeyService billKeyService;
    private final AuthenticationService authenticationService;
    private final TradeRepository tradeRepository;
    private final TradeFailService tradeFailService;
    private final JsonUtil jsonUtil;
    private final GiftCardCommonService giftCardCommonService;

    @Value("${giftcard.single.issue.stl-mid}")
    private String issMid;

    @PersistenceContext
    private EntityManager entityManager;

    private final JdbcTemplate jdbcTemplate;

    public GiftCardIssueVo validateAndConvertRequestDto(GiftCardIssueRequestDto dto) {
        // 입력값 검증
        CustomerDto customerDto = dto.getCustomerDto();
        String mId = customerDto.getMid();

        //잔액 사용순서 검증
        String blcUseOrd = "P";
        if (StringUtils.isNotEmpty(dto.getBlcUseOrd())) {
            blcUseOrd = dto.getBlcUseOrd().toUpperCase();
        }

        if (!"P".equals(blcUseOrd) && !"M".equals(blcUseOrd)) {
            throw new RequestValidationException(ErrorCode.INVALID_BLC_USE_ORD);
        }

        // 금액 검증
        long trdAmt = Long.parseLong(dto.getTrdAmt());
        long mnyBlc = Long.parseLong(dto.getMnyBlc());
        long pntBlc = Long.parseLong(dto.getPntBlc());
        long inBlc = mnyBlc + pntBlc;
        if (trdAmt < 0 || mnyBlc < 0 || pntBlc < 0) {
            throw new RequestValidationException(ErrorCode.AMT_CANNOT_BE_NEGATIVE);
        }

        // 상품권 1회 최대 발행 건수 확인
        if (Integer.parseInt(dto.getTotCnt()) > 50) {
            throw new RequestValidationException(ErrorCode.GIFT_CARD_ISSUE_LIMIT_EXCEEDED);
        }

        // 상품권 발행 요청 검증
        int gcTotAmt = 0;
        int gcTotCnt = 0;
        List<Map<String, String>> gcReqPktList = new ArrayList<>();
        List<GiftCardIssueInfo> gcInfoList = dto.getIssList();
        for (GiftCardIssueInfo gcInfo : gcInfoList) {
            //상품권 권종 및 금액 유효성 검사
            if (StringUtils.isBlank(gcInfo.getGcAmt())) {
                throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, "발행 금액");
            }
            if (StringUtils.isBlank(gcInfo.getGcQty())) {
                throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, "발행 수량");
            }

            // 상품권 권종 확인
            if (!GiftCardAmountType.isValid(gcInfo.getGcAmt())) { // 권종 확인
                log.info("Not support gift card amount type. req is {}", gcInfo.getGcAmt());
                throw new RequestValidationException(ErrorCode.GIFT_CARD_UNSUPPORTED_AMOUNT);
            }

            // 총 발행 건수 및 발행 금액 계산
            gcTotCnt += Integer.parseInt(gcInfo.getGcQty());
            gcTotAmt += Integer.parseInt(gcInfo.getGcAmt()) * Integer.parseInt(gcInfo.getGcQty()); // 권종 x 수량

            // 선불 거래 테이블(MPS_TRD.GC_REQ_PKT)에 저장할 상품권 발행 전문
            Map<String, String> gcReqPkt = new HashMap<>();
            gcReqPkt.put(gcInfo.getGcAmt(), gcInfo.getGcQty());
            gcReqPktList.add(gcReqPkt);
        }

        // 발행 요청 건수 확인
        if (gcTotCnt != Integer.parseInt(dto.getTotCnt())) {
            log.info("Invalid gift card issue count. req={}, sum={}", dto.getTotCnt(), gcTotCnt);
            throw new RequestValidationException(ErrorCode.GIFT_CARD_INVALID_REQUEST_TOTAL_COUNT);
        }

        // 발행 요청 금액 확인
        if (gcTotAmt != trdAmt) {
            log.info("Invalid gift card issue amount. req={}, sum={}", trdAmt, gcTotAmt);
            throw new RequestValidationException(ErrorCode.GIFT_CARD_INVALID_REQUEST_TOTAL_AMOUNT);
        }

        // 서버시간은 동일하게
        LocalDateTime now = DateTimeUtil.getCurrentDateTime();
        String trdDt = DateTimeUtil.fromLocalDate(now.toLocalDate(), DateTimeUtil.YYYYMMDD);
        String trdTm = DateTimeUtil.fromLocalTime(now.toLocalTime(), DateTimeUtil.HHMMSS);

        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(dto.getReqDt()) && StringUtils.isNotEmpty(dto.getReqTm())) {
            mReqDtm = dto.getReqDt() + dto.getReqTm();
        }

        // 유효기간 설정
        String vldDt = LocalDate.now().plusYears(5).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        if (!StringUtils.isEmpty(dto.getVldDt())) {
//            vldDt = dto.getVldDt();
//        }

        // 거래번호 생성 (선불 거래번호 = 발행거래번호)
        String trdNo = giftCardCommonService.generateTradeNo();

        return GiftCardIssueVo.builder()
                .mId(mId) // 고객 상점 아이디
                .mpsCustNo(dto.getCustNo()) // 고객 번호
                .trdDivCd(TrdDivCd.GIFT_CARD_ISSUE.getTrdDivCd()) // 거래 구분 코드
                .reqIssInfoList(dto.getIssList()) // 상품권 발행 요청 정보
                .vldDt(vldDt) // 유효기간 (기본 5년)
                .mTrdNo(dto.getMTrdNo()) // 상점 거래 번호
                .blcUseOrd(blcUseOrd) // 잔액 구분 코드
                .trdNo(trdNo) // 선불 거래 번호 = 상품권 발행 거래 번호
                .trdDt(trdDt) // 거래 일자
                .trdTm(trdTm) // 거래 시간
                .trdAmt(trdAmt) // 총 발행 금액
                .mnyBlc(mnyBlc) // 머니 잔액
                .pntBlc(pntBlc) // 포인트 잔액
                .inBlc(inBlc) // 머니 + 포인트 잔액
                .mCustId(customerDto.getMCustId()) // 고객 아이디
                .mReqDtm(mReqDtm) // 상점 요청 일시
                .stlMid(issMid) // 정산상점 아이디 (= 발행처 상점 아이디)
                .useMid(dto.getUseMid()) // 사용처 상점 아이디
                .mResrvField1(dto.getMResrvField1()) // 예비필드1
                .mResrvField2(dto.getMResrvField2()) // 예비필드2
                .mResrvField3(dto.getMResrvField3()) // 예비필드3
                .trdSumry(dto.getTrdSumry()) // 거래적요
                .gcReqPktList(gcReqPktList) // 상품권 발행 요청 전문 (권종 : 건수)
                .createDate(now) // 서버 생성 시간
                .build();
    }

    public MpsMarket getMpsMarket(String mId) {
        return mpsMarketRepository.findMpsMarketByMid(mId).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));
    }

    public ChkPinErrorCountResponseDto checkPinNumber(String trdNo, String pinNo, MpsMarket mpsMarket, CustomerDto customerDto) {
        boolean isBillKeyUsed = "Y".equals(mpsMarket.getBillKeyUseYn());
        boolean hasBillKey = !CommonUtil.nullTrim(customerDto.getBillKeyEnc()).isEmpty();
        boolean isPinFormat = StringUtils.isNotBlank(pinNo) && pinNo.length() == 6;

        ChkPinErrorCountRequestDto chkPinErrorCountRequestDto = ChkPinErrorCountRequestDto.builder()
                .pin(pinNo)
                .trdNo(trdNo)
                .customerDto(customerDto)
                .mpsMarket(mpsMarket)
                .build();

        ChkPinErrorCountResponseDto chkPinErrorCountResponseDto;

        if (isBillKeyUsed && hasBillKey && !isPinFormat) {
            chkPinErrorCountResponseDto = billKeyService.isCorrectBillkey(chkPinErrorCountRequestDto);
        } else {
            chkPinErrorCountResponseDto = authenticationService.isCorrectWhiteLabelPin(chkPinErrorCountRequestDto);
        }

        return chkPinErrorCountResponseDto;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertTradeFail(GiftCardIssueVo issueVo, String errCd, String errMsg) {
        TradeFailInsertDto trdFailDto = TradeFailInsertDto.builder()
                .blcUseOrd(issueVo.getBlcUseOrd())//잔액사용순서
                .trdNo(issueVo.getTrdNo())//거래번호
                .trdDivCd(issueVo.getTrdDivCd())//거래구분코드
                .mid(issueVo.getMId()) // 고객 상점 아이디
                .amtSign(-1) // 금액 부호 (마이너스는 사용)
                .trdAmt(issueVo.getTrdAmt()) // 총 발행 금액
                .mnyBlc(issueVo.getMnyBlc()) // 머니 잔액
                .pntBlc(issueVo.getPntBlc()) // 포인트 잔액
                .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd()) // 충전수단
                .reqDtm(issueVo.getMReqDtm()) // 상점 요청 일시
                .mTrdNo(issueVo.getMTrdNo()) // 상점 거래 번호
                .custNo(issueVo.getMpsCustNo()) // 고객 번호
                .mCustId(issueVo.getMCustId()) // 고객 아이디
                .csrcIssReqYn("N") // 상품권은 현금영수증 N
                .csrcIssStatCd("N") // 상품권은 현금영수증 N
                .failDt(issueVo.getTrdDt())//거래일자
                .failTm(issueVo.getTrdTm())//거래시간
                .stlMId(issueVo.getStlMid()) // 정산상점 아이디 (=발행처 상점 아이디)
                .errCd(errCd) // 실패코드
                .errMsg(errMsg) // 실패메시지
                .storCd("N") // 사용처 코드 N
                .storNm("N") // 사용처 코드 N
                .prdtCd(MpsPrdtCd.use.getPrdtCd()) // 상품코드 (PUSE)
                .trdSumry(issueVo.getTrdSumry()) // 거래적요
                .mResrvField1(issueVo.getMResrvField1()) // 예비필드1
                .mResrvField2(issueVo.getMResrvField2()) // 예비필드2
                .mResrvField3(issueVo.getMResrvField3()) // 예비필드3
                .gcPktReq(jsonUtil.toJson(issueVo.getGcReqPktList())) // 상품권 발행 요청 전문 (권종 : 건수)
                .build();
        tradeFailService.insertTradeFail(trdFailDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public UseOut useBalance(GiftCardIssueVo issueVo) {
        UseIn useInParam = UseIn.builder()
                .inMpsCustNo(issueVo.getMpsCustNo())
                .inTrdDivCd(TrdDivCd.COMMON_USE.getTrdDivCd())
                .inBlcUseOrd(issueVo.getBlcUseOrd())
                .inUseTrdNo(issueVo.getTrdNo())
                .inUseTrdDt(issueVo.getTrdDt())
                .inTrdAmt(issueVo.getTrdAmt())
                .inBlc(issueVo.getInBlc())
                .inWorkerID(ServerInfoConfig.HOST_NAME)
                .inWorkerIP(ServerInfoConfig.HOST_IP)
                .build();
        return tradeRepository.use(useInParam);
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertTradeSuccess(GiftCardIssueVo issueVo, UseOut useOut) {
        long outMnyAmt = useOut.getOutMnyAmt();
        long outPntAmt = useOut.getOutPntAmt();
        long outMnyBlc = useOut.getOutMnyBlc();
        long outPntBlc = useOut.getOutPntBlc();
        long outWaitMnyBlc = useOut.getOutWaitMnyBlc();

        Trade trade = Trade.builder()
                .trdNo(issueVo.getTrdNo())//거래번호
                .trdDt(issueVo.getTrdDt())//거래일자
                .trdTm(issueVo.getTrdTm())//거래시간
                .trdDivCd(issueVo.getTrdDivCd())//거래구분코드 (IS)
                .svcCd(MpsApiCd.SVC_CD) // 서비스코드
                .prdtCd(MpsPrdtCd.use.getPrdtCd()) // 상품코드
                .mid(issueVo.getMId()) // 고객 상점 아이디
                .blcUseOrd(issueVo.getBlcUseOrd())//잔액사용순서
                .amtSign(-1)//사용부호 사용이므로 -
                .trdAmt(issueVo.getTrdAmt())//거래금액
                .mnyAmt(outMnyAmt)//프로시저 결과값내 머니 사용금액
                .pntAmt(outPntAmt)//프로시저 결과값내 포인트 사용금액
                .waitMnyAmt(0)//대기머니 금액 X
                .mnyBlc(outMnyBlc)//프로시저 결과값내 머니 잔액
                .pntBlc(outPntBlc)//프로시저 결과값내 포인트 잔액
                .waitMnyBlc(outWaitMnyBlc)//프로시저 결과값내 대기머니 잔액
                .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())//충전수단 코드
                .mReqDtm(issueVo.getMReqDtm())//가맹점요청일시
                .mTrdNo(issueVo.getMTrdNo())//가맹점 요청거래번호
                .mpsCustNo(issueVo.getMpsCustNo())//회원번호
                .mCustId(issueVo.getMCustId()) //회원아이디
                .csrcIssReqYn("N")//현금영수증 발행 Yn
                .stlMId(issueVo.getStlMid())//정산 상점 아이디
                .storCd("N")
                .storNm("N")
                .trdSumry(issueVo.getTrdSumry())
                .mResrvField1(issueVo.getMResrvField1())
                .mResrvField2(issueVo.getMResrvField2())
                .mResrvField3(issueVo.getMResrvField3())
                .createdIp(ServerInfoConfig.HOST_IP)
                .createdId(ServerInfoConfig.HOST_NAME)
                .createdDate(issueVo.getCreateDate())
                .gcReqPkt(jsonUtil.toJson(issueVo.getGcReqPktList())) // 상품권 전문 저장
                .build();
        tradeRepository.save(trade);

        // for error logging
        entityManager.flush();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<GiftCardIssueInfo> issueGiftCard(GiftCardIssueVo issueVo) {
        Optional<MarketAddInfo> optionalMarketAddInfo = giftCardCommonService.getMarketAddInfoByMId(issueVo.getMId());
        if (!optionalMarketAddInfo.isPresent()) {
            throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
        }

        MarketAddInfoDto marketAddInfo = MarketAddInfoDto.of(optionalMarketAddInfo.get());

        List<GiftCardIssueInfo> reqIssInfoList = issueVo.getReqIssInfoList();

        List<String> gcNoEncList = new ArrayList<>();
        List<GiftCardIssue> gcIssInfoList = new ArrayList<>();
        for (GiftCardIssueInfo reqIssInfo : reqIssInfoList) {
            int amount = Integer.parseInt(reqIssInfo.getGcAmt());
            int quantity = Integer.parseInt(reqIssInfo.getGcQty());

            List<String> gcNoList = new ArrayList<>();
            for (int i = 0; i < quantity; i++) {
                String gcNo = giftCardCommonService.generateGiftCardNo(issueVo.getUseMid());
                gcNoList.add(gcNo); // for response

                String gcNoEnc = giftCardCommonService.encrypt(gcNo);
                gcNoEncList.add(gcNoEnc); // for logging

                String gcNoMsk = giftCardCommonService.maskingGiftCardNo(gcNo);
                GiftCardIssue gcIssue = GiftCardIssue.builder()
                        .gcNoEnc(gcNoEnc)
                        .issDt(issueVo.getTrdDt())
                        .gcNoMsk(gcNoMsk)
                        .issTrdNo(issueVo.getTrdNo())
                        .issAmt(amount)
                        .blc(amount)
                        .vldPd(issueVo.getVldDt())
                        .useMid(issueVo.getUseMid())
                        .gcStatCd(GcStatCd.ISSUE.getCode())
                        .createdDate(issueVo.getCreateDate())
                        .createdIp(ServerInfoConfig.HOST_IP)
                        .createdId(ServerInfoConfig.HOST_NAME)
                        .build();

                gcIssInfoList.add(gcIssue);
            }

            // 발행 요청 정보에 상품권 번호 목록 추가
            try {
                reqIssInfo.setGcNoList(CipherUtil.encrypt(jsonUtil.toJson(gcNoList), marketAddInfo.getEncKey()));
            } catch (Exception ex) {
                throw new RequestValidationException(ErrorCode.ENCRYPT_ERROR);
            }
        }

        log.info("상품권 발행거래번호={}, 상품권 번호 목록={}", issueVo.getTrdNo(), gcNoEncList);
        log.info("상품권 발행 요청 저장 데이터={}", gcIssInfoList);

        String sql = "INSERT INTO MPS.PM_MPS_GC_ISS (" +
                "GC_NO_ENC, " +
                "ISS_DT, " +
                "GC_NO_MSK, " +
                "ISS_TRD_NO, " +
                "ISS_AMT, " +
                "BLC, " +
                "VLD_PD, " +
                "USE_M_ID, " +
                "GC_STAT_CD, " +
                "INST_DATE, " +
                "INST_ID, " +
                "INST_IP" +
                ") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                GiftCardIssue gcIssue = gcIssInfoList.get(i);
                ps.setString(1, gcIssue.getGcNoEnc());
                ps.setString(2, gcIssue.getIssDt());
                ps.setString(3, gcIssue.getGcNoMsk());
                ps.setString(4, gcIssue.getIssTrdNo());
                ps.setLong(5, gcIssue.getIssAmt());
                ps.setLong(6, gcIssue.getBlc());
                ps.setString(7, gcIssue.getVldPd());
                ps.setString(8, gcIssue.getUseMid());
                ps.setString(9, gcIssue.getGcStatCd());
                ps.setObject(10, gcIssue.getCreatedDate());
                ps.setString(11, gcIssue.getCreatedId());
                ps.setString(12, gcIssue.getCreatedIp());
            }

            @Override
            public int getBatchSize() {
                return gcIssInfoList.size();
            }
        });

        return reqIssInfoList;
    }
}
