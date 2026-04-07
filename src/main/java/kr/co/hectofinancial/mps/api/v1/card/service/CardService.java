package kr.co.hectofinancial.mps.api.v1.card.service;

import kr.co.hectofinancial.mps.api.v1.card.domain.BpcCust;
import kr.co.hectofinancial.mps.api.v1.card.dto.*;
import kr.co.hectofinancial.mps.api.v1.card.repository.CardRepository;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.service.CardNotiService;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.notification.dto.PyNtcSendInsertRequestDto;
import kr.co.hectofinancial.mps.api.v1.notification.service.NotiService;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeFailInsertDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.CustomerWalletResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletCancelResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Use.UseIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Use.UseOut;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseCancel.UseCancelIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseCancel.UseCancelOut;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.api.v1.trade.service.TradeFailService;
import kr.co.hectofinancial.mps.api.v1.trade.service.wallet.WalletService;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.*;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static kr.co.hectofinancial.mps.global.util.CipherSha256Util.digestSHA256;
import static kr.co.hectofinancial.mps.global.util.CipherUtil.encrypt;


@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    @Value("${mps.card.use-stlmid}")
    private String useStlMid;
    @Value("${mps.card.reissue-stlmid}")
    private String reissueStlMid;
    private final TradeRepository tradeRepository;
    private final SequenceService sequenceService;
    private final TradeFailService tradeFailService;
    private final NotiService notiService;
    private final String SERVER_IP = ServerInfoConfig.HOST_IP;
    private final String SERVER_ID = ServerInfoConfig.HOST_NAME;
    private final CardRepository cardRepository;
    private final WalletService walletService;
    private final CommonService commonService;
    private final CardNotiService cardNotiService;

    @Transactional(rollbackFor = Exception.class)
    public CardUseApprovalResponseDto cardUseApproval(CardUseApprovalRequestDto cardUseApprovalRequestDto) {

        //잔액사용순서 default P
        String blcUseOrd = "P";
        if (StringUtils.isNotEmpty(cardUseApprovalRequestDto.getBlcUseOrd())) {
            blcUseOrd = cardUseApprovalRequestDto.getBlcUseOrd().toUpperCase();
        }

        //잔액 사용순서 검증
        if (!blcUseOrd.equals("P") && !blcUseOrd.equals("M")) {
            throw new RequestValidationException(ErrorCode.INVALID_BLC_USE_ORD);
        }

        //카드사용가맹점 검증
        commonService.checkValidMidForCardUse(TrdDivCd.COMMON_USE.getTrdDivCd(), cardUseApprovalRequestDto.getCustomerDto().getMid(), cardUseApprovalRequestDto.getStorCd());

        Set<String> cnclTypeList = new HashSet<>(Arrays.asList(CnclTypeCd.CARD_REISSUE.getCnclTypeCd(), CnclTypeCd.UNAPPROVAL.getCnclTypeCd(), CnclTypeCd.CANCEL.getCnclTypeCd()));
        //BPO 카드 재발급비용으로 사용될때는 CNCL_TYPE_CD = 1 (당일사용취소만 가능함)
        if(!CommonUtil.nullTrim(cardUseApprovalRequestDto.getCnclTypeCd()).equals("")){
            if (!cnclTypeList.contains(cardUseApprovalRequestDto.getCnclTypeCd())) {
                throw new RequestValidationException(ErrorCode.PARAM_INVALID, "취소유형코드 오류입니다.");
            }
        }

        //회원 추출
        CustomerDto customerDto = cardUseApprovalRequestDto.getCustomerDto();

        //변수 선언
        String trdNo = sequenceService.generateTradeSeq01();
        String mTrdNo = cardUseApprovalRequestDto.getMTrdNo();

        CustomDateTimeUtil today = new CustomDateTimeUtil();
        String trdDt = today.getDate();
        String trdTm = today.getTime();

        String mpsCustNo = customerDto.getMpsCustNo();
        String mId = customerDto.getMid();
        String mCustId = customerDto.getMCustId();
        String trdDivCd = TrdDivCd.COMMON_USE.getTrdDivCd(); //고정
        BpcCust tgtBpcCust = null;

        long trdAmt = Long.parseLong(cardUseApprovalRequestDto.getTrdAmt());
        long mnyBlc = Long.parseLong(cardUseApprovalRequestDto.getMnyBlc());
        long pntBlc = Long.parseLong(cardUseApprovalRequestDto.getPntBlc());
        long inBlc = mnyBlc + pntBlc;

        String storCd = StringUtils.isEmpty(cardUseApprovalRequestDto.getStorCd()) ? "N" : cardUseApprovalRequestDto.getStorCd();
        String storNm = StringUtils.isEmpty(cardUseApprovalRequestDto.getStorNm()) ? "N" : URLDecoder.decode(cardUseApprovalRequestDto.getStorNm());

        String stlMId = cardUseApprovalRequestDto.getStlMId();
        if (!stlMId.equals(useStlMid) && !stlMId.equals(reissueStlMid)) {
            throw new RequestValidationException(ErrorCode.INVALID_STL_MID);
        }

        if(stlMId.equals(useStlMid)){ // 재발급거래는 CARD_MNG_NO를 검증 할수없음 카드승인거래일때만 CARD_MNG_NO 검증
            if(CommonUtil.nullTrim(cardUseApprovalRequestDto.getCardMngNo()) == null){
                throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, "CARD_MNG_NO");
            }

            //todo 앞단에서 카드사용 정산상점아이디로 들어왔을때 국내 결제 정지 여부 컬럼 봐야함 (DMST_PMT_BLK_YN)

            List<String> cardStatCdList = new ArrayList<>(Arrays.asList("H")); //카드분실신고 상태
            List<String> cardLastDivCdList = new ArrayList<>(Arrays.asList("2")); //카드탈회
            tgtBpcCust = cardRepository.findByCardMngNoAndCardStatCdNotInAndLastCardDivCdNotIn(cardUseApprovalRequestDto.getCardMngNo(), cardStatCdList, cardLastDivCdList);
            if (tgtBpcCust == null) {
                throw new RequestValidationException(ErrorCode.CARD_INFO_NOT_FOUND);
            }else{
                /* 국내결제정지여부 검증 */
                log.info("국내결제정지여부: [{}]", tgtBpcCust.getDmstPmtBlkYn());
                if(tgtBpcCust.getDmstPmtBlkYn().equals("N")){
                    throw new RequestValidationException(ErrorCode.CARD_INFO_NOT_FOUND, "(국내이용정지 카드)");
                }
                /* 카드상태 검증 */
                log.info("카드상태: [{}]", tgtBpcCust.getCardStatCd());
                if(!mpsCustNo.equals(tgtBpcCust.getMpsCustNo())){
                    throw new RequestValidationException(ErrorCode.CARD_INFO_NOT_FOUND, "(카드상태 확인필요)");
                }
            }
        }

        //금액 음수 검증
        if (trdAmt < 0 || mnyBlc < 0 || pntBlc < 0) {
            throw new RequestValidationException(ErrorCode.AMT_CANNOT_BE_NEGATIVE);
        }
        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(cardUseApprovalRequestDto.getReqDt()) && StringUtils.isNotEmpty(cardUseApprovalRequestDto.getReqTm())) {
            mReqDtm = cardUseApprovalRequestDto.getReqDt() + cardUseApprovalRequestDto.getReqTm();
        }

        //프로시저 호출을 위해 In변수 담는 객체 Build
        UseIn useInParam = UseIn.builder()
                .inMpsCustNo(mpsCustNo)
                .inTrdDivCd(trdDivCd)
                .inBlcUseOrd(blcUseOrd)
                .inUseTrdNo(trdNo)
                .inUseTrdDt(trdDt)
                .inTrdAmt(trdAmt)
                .inBlc(inBlc)
                .inWorkerID(SERVER_ID)
                .inWorkerIP(SERVER_IP)
                .build();
        UseOut useOut = tradeRepository.use(useInParam);

        long resCode = useOut.getOutResCd();
        String resMsg = useOut.getOutResMsg();
        log.info("선불금 사용 EXEC => 회원번호:[{}] 결과코드:[{}] 결과메세지:[{}]", mpsCustNo, resCode, resMsg);

        if (resCode == ProcResCd.SUCCESS.getResCd()) {
            //거래 성공 쌓기

            long outMnyAmt = useOut.getOutMnyAmt();
            long outPntAmt = useOut.getOutPntAmt();
            long outMnyBlc = useOut.getOutMnyBlc();
            long outPntBlc = useOut.getOutPntBlc();
            long outWaitMnyBlc = useOut.getOutWaitMnyBlc();

            /* 포인트만 사용시에 현금영수증 발행 N */
            Trade trade = Trade.builder()
                    .trdNo(trdNo)//거래번호
                    .trdDt(trdDt)//거래일자
                    .trdTm(trdTm)//거래시간
                    .trdDivCd(trdDivCd)//거래구분코드
                    .svcCd(MpsApiCd.SVC_CD)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .mid(mId)
                    .blcUseOrd(blcUseOrd)//잔액사용순서
                    .amtSign(-1)//사용부호 사용이므로 -
                    .trdAmt(trdAmt)//거래금액
                    .mnyAmt(outMnyAmt)//프로시저 결과값내 머니 사용금액
                    .pntAmt(outPntAmt)//프로시저 결과값내 포인트 사용금액
                    .waitMnyAmt(0)//대기머니 금액 X
                    .mnyBlc(outMnyBlc)//프로시저 결과값내 머니 잔액
                    .pntBlc(outPntBlc)//프로시저 결과값내 포인트 잔액
                    .waitMnyBlc(outWaitMnyBlc)//프로시저 결과값내 대기머니 잔액
                    .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())//충전수단 코드
                    .mReqDtm(mReqDtm)//가맹점요청일시
                    .mTrdNo(mTrdNo)//가맹점 요청거래번호
                    .mpsCustNo(mpsCustNo)//회원번호
                    .mCustId(mCustId) //회원아이디
                    .csrcIssReqYn("N")//현금영수증 발행 Yn
                    .csrcIssStatCd("N")//현금영수증 발행 상태 N
                    .stlMId(stlMId)//정산 상점 아이디
                    .storCd(storCd)
                    .storNm(storNm)
                    .trdSumry(cardUseApprovalRequestDto.getTrdSumry())
                    .mResrvField1(cardUseApprovalRequestDto.getMResrvField1())
                    .mResrvField2(cardUseApprovalRequestDto.getMResrvField2())
                    .mResrvField3(cardUseApprovalRequestDto.getMResrvField3())
                    .cardMngNo(cardUseApprovalRequestDto.getCardMngNo())
                    .cnclTypeCd(cardUseApprovalRequestDto.getCnclTypeCd())
                    .createdIp(SERVER_IP)
                    .createdId(SERVER_ID)
                    .build();
            tradeRepository.save(trade);

            /* 사용 성공 충전 알림 메일 전송 */
            if (!CommonUtil.nullTrim(customerDto.getEmail()).equals("")) {
                try {
                    String trdDtm;
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    trdDtm = now.format(formatter);

                    String formatTrdAmt = CommonUtil.formatMoney(trdAmt);
                    String formatOutMnyAmt = CommonUtil.formatMoney(outMnyAmt);
                    String formatOutPntAmt = CommonUtil.formatMoney(outPntAmt);

                    PyNtcSendInsertRequestDto pyNtcSendInsertRequestDto = PyNtcSendInsertRequestDto.builder()
                            .mpsCustNo(mpsCustNo)
                            .custNm(customerDto.getCustNm())
                            .email(customerDto.getEmail())
                            .trdNo(trdNo)
                            .amt(formatTrdAmt)
                            .mnyAmt(formatOutMnyAmt)
                            .pntAmt(formatOutPntAmt)
                            .trdDtm(trdDtm)
                            .mid(mId)
                            .storNm(storNm)
                            .stlMid(mId)
                            .msgTmplId("MPS_TRD_04")
                            .build();
                    notiService.savePyNtcSend(pyNtcSendInsertRequestDto);
                } catch (Exception e) {
                    log.error("[사용 알림 메일 발송 실패] 회원번호: [{}], 거래번호: [{}]", mpsCustNo, trdNo);
                    String message = String.format("[선불회원번호: " + mpsCustNo + ", 거래번호: " + trdNo + "]");
                    MonitAgent.sendMonitAgent(ErrorCode.NOTI_SERVICE_FAIL.getErrorCode(), message);
                    e.printStackTrace();
                }
            }

            return CardUseApprovalResponseDto.builder()
                    .custNo(mpsCustNo)
                    .mTrdNo(mTrdNo)
                    .trdNo(trdNo)
                    .trdDt(trdDt)
                    .trdTm(trdTm)
                    .trdAmt(String.valueOf(trdAmt))
                    .mnyAmt(String.valueOf(outMnyAmt))
                    .pntAmt(String.valueOf(outPntAmt))
                    .mnyBlc(String.valueOf(outMnyBlc))
                    .pntBlc(String.valueOf(outPntBlc))
                    .build();
        } else {
            log.info("선불금 사용 **실패** => 회원번호:[{}] 결과코드:[{}] 결과메세지:[{}]", mpsCustNo, resCode, resMsg);

            ErrorCode errorCode = ErrorCode.SERVER_ERROR_CODE;

            if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
                errorCode = ErrorCode.BALANCE_NOT_MATCHED;
            } else if (resCode == ProcResCd.REQ_AMT_NOT_MATCHED.getResCd()) {
                errorCode = ErrorCode.REQ_AMT_NOT_MATCHED;
            } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd()) {
                errorCode = ErrorCode.TRADE_AMT_ERROR;
            } else if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
                throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
            }

            TradeFailInsertDto trdFailDto = TradeFailInsertDto.builder()
                    .trdNo(trdNo)//거래번호
                    .trdDivCd(trdDivCd)//거래구분코드
                    .blcUseOrd(blcUseOrd)
                    .mid(mId)
                    .amtSign(-1)
                    .trdAmt(trdAmt)
                    .mnyBlc(mnyBlc)
                    .pntBlc(pntBlc)
                    .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                    .reqDtm(mReqDtm)
                    .mTrdNo(mTrdNo)
                    .custNo(mpsCustNo)
                    .mCustId(mCustId)
                    .csrcIssReqYn("N")
                    .csrcIssStatCd("N")
                    .failDt(trdDt)//거래일자
                    .failTm(trdTm)//거래시간
                    .stlMId(stlMId)
                    .errCd(String.valueOf(resCode))
                    .errMsg(resMsg)
                    .storCd(storCd)
                    .storNm(storNm)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .trdSumry(cardUseApprovalRequestDto.getTrdSumry())
                    .mResrvField1(cardUseApprovalRequestDto.getMResrvField1())
                    .mResrvField2(cardUseApprovalRequestDto.getMResrvField2())
                    .mResrvField3(cardUseApprovalRequestDto.getMResrvField3())
                    .cardMngNo(cardUseApprovalRequestDto.getCardMngNo())
                    .build();
            tradeFailService.insertTradeFail(trdFailDto);

            throw new RequestValidationException(errorCode);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public WalletCancelResponseDto cancelWallet(CardUseCancelRequestDto cardUseCancelRequestDto) {

        //todo 삼성카드 취소 키 (원거래처리일시+원거래승인번호+원거래승인금액+카드번호+가맹점번호)
        CustomDateTimeUtil today = new CustomDateTimeUtil();
        String trdDt = today.getDate();
        String trdTm = today.getTime();
        BpcCust tgtBpcCust = null;

        //원거래 조회
        Trade orgTrade = tradeRepository.findTradeByMpsCustNoAndTrdNoAndTrdDt(cardUseCancelRequestDto.getCustNo(), cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt())
                .orElseThrow(() -> new RequestValidationException(ErrorCode.ORG_TRADE_INFO_NOT_FOUND));

        //카드사용가맹점 검증
        commonService.checkValidMidForCardUse(TrdDivCd.USE_COMMON_CANCEL.getTrdDivCd(), cardUseCancelRequestDto.getCustomerDto().getMid(), orgTrade.getStorCd());

        if (CommonUtil.nullTrim(cardUseCancelRequestDto.getCnclMnyAmt()).equals("") || CommonUtil.nullTrim(cardUseCancelRequestDto.getCnclPntAmt()).equals("")) {
            throw new RequestValidationException(ErrorCode.USE_CANCEL_AMT_ERROR);
        }

        long cancelMnyAmt = Long.parseLong(cardUseCancelRequestDto.getCnclMnyAmt()); //취소 요청 머니 금액
        long cancelPntAmt = Long.parseLong(cardUseCancelRequestDto.getCnclPntAmt()); //취소 요청 포인트 금액

        //원거래건의 거래구분코드 "사용" 검증
        if (!TrdDivCd.COMMON_USE.getTrdDivCd().equals(orgTrade.getTrdDivCd())) {
            log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] TRD_DIV_CD:[{}] 원거래건의 거래구분 코드가 [사용]이 아님", cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt(), orgTrade.getTrdDivCd());
            throw new RequestValidationException(ErrorCode.NOT_VALID_TRD_DIV_CD);
        }
        //이전 취소건 검증
        if (orgTrade.getTrdAmt() == orgTrade.getCnclTrdAmt()) {
            log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 기취소건에 대한 취소 요청 인입", cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt());
            throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_CANCELED);
        }

        if (orgTrade.getTrdAmt() < (orgTrade.getCnclTrdAmt() + cancelMnyAmt + cancelPntAmt)) {
            log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 취소요청 금액이 원거래금액보다 큼(전체)", cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt());
            throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
        }
        if (orgTrade.getMnyAmt() < (orgTrade.getCnclMnyAmt() + cancelMnyAmt)) {
            log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 취소요청 금액이 원거래금액보다 큼(머니)", cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt());
            throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
        }
        if (orgTrade.getPntAmt() < (orgTrade.getCnclPntAmt() + cancelPntAmt)) {
            log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 취소요청 금액이 원거래금액보다 큼(포인트)", cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt());
            throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
        }

        String cnclTypeCd = orgTrade.getCnclTypeCd();
        log.info("cnclTypeCd : [{}]", cnclTypeCd);
        if(CnclTypeCd.CARD_REISSUE.getCnclTypeCd().equals(cnclTypeCd)){
            log.info(">>>>>>>>> 선불카드 재발급 취소 거래 인입");

            //BPO카드 재발급 사용건 당일취소만 가능하도록
            if(!orgTrade.getTrdDt().equals(trdDt)){ //가맹점요청시간이 아닌 HF기준
                throw new RequestValidationException(ErrorCode.TRADE_CANCELED_FAIL, "카드 재발급 취소는 당일취소만 가능해요.");
            }

            //재발급 거래 이미 취소된건이 있거나, 부분취소 검증
            if(orgTrade.getCnclTrdAmt() > 0 || (cancelMnyAmt + cancelPntAmt) != orgTrade.getTrdAmt()){ //BPO 재발급 비용 부분취소안됨
                throw new RequestValidationException(ErrorCode.TRADE_CANCELED_FAIL);
            }

        }else{

            //카드조회
            tgtBpcCust = cardRepository.findByCardMngNo(cardUseCancelRequestDto.getCardMngNo());
            if (tgtBpcCust == null) {
                throw new RequestValidationException(ErrorCode.CARD_INFO_NOT_FOUND);
            }else{
                log.info("카드상태: [{}]", tgtBpcCust.getCardStatCd());
                if(!cardUseCancelRequestDto.getCustNo().equals(tgtBpcCust.getMpsCustNo())){
                    throw new RequestValidationException(ErrorCode.CARD_INFO_NOT_FOUND);
                }
            }

            cnclTypeCd = cardUseCancelRequestDto.getCnclTypeCd();

            if (orgTrade.getTrdAmt() < (orgTrade.getCnclTrdAmt() + cancelMnyAmt + cancelPntAmt)) {
                log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 취소요청 금액이 원거래금액보다 큼(전체)", cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt());
                throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
            }
            if (orgTrade.getMnyAmt() < (orgTrade.getCnclMnyAmt() + cancelMnyAmt)) {
                log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 취소요청 금액이 원거래금액보다 큼(머니)", cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt());
                throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
            }
            if (orgTrade.getPntAmt() < (orgTrade.getCnclPntAmt() + cancelPntAmt)) {
                log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 취소요청 금액이 원거래금액보다 큼(포인트)", cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt());
                throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
            }
        }

        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(cardUseCancelRequestDto.getReqDt()) && StringUtils.isNotEmpty(cardUseCancelRequestDto.getReqTm())) {
            mReqDtm = cardUseCancelRequestDto.getReqDt() + cardUseCancelRequestDto.getReqTm();
        }

        //회원추출
        CustomerDto customerDto = cardUseCancelRequestDto.getCustomerDto();

        //변수 선언
        String trdNo = sequenceService.generateTradeSeq01();

        String mTrdNo = cardUseCancelRequestDto.getMTrdNo();
        String mpsCustNo = customerDto.getMpsCustNo();
        String mCustId = customerDto.getMCustId();
        String mId = customerDto.getMid();
        String trdDivCd = TrdDivCd.USE_COMMON_CANCEL.getTrdDivCd();
        String trdSumry = cardUseCancelRequestDto.getTrdSumry();

        long inBlc = Long.parseLong(cardUseCancelRequestDto.getMnyBlc()) + Long.parseLong(cardUseCancelRequestDto.getPntBlc());

        String orgTrdNo = orgTrade.getTrdNo();
        String orgTrdDt = orgTrade.getTrdDt();

        String storNm = orgTrade.getStorNm();
        String storCd = orgTrade.getStorCd();

        //사용 취소 프로시저 호출 -> 사용취소 및 대기머니 지급
        UseCancelIn userCancelIn = UseCancelIn.builder()
                .inMpsCustNo(mpsCustNo)
                .inTrdDivCd(trdDivCd)
                .inCustDivCd(customerDto.getCustDivCd())
                .inChrgLmtAmt(customerDto.getChrgLmtAmlt())
                .inUseTrdNo(orgTrdNo)
                .inUseTrdDt(orgTrdDt)
                .inCnclTrdNo(trdNo)
                .inCnclTrdDt(trdDt)
                .inMnyAmt(cancelMnyAmt)
                .inPntAmt(cancelPntAmt)
                .inBlc(inBlc)
                .inMID(customerDto.getMid())
                .inWorkerID(SERVER_ID)
                .inWorkerIP(SERVER_IP)
                .build();

        UseCancelOut useCancelOut = tradeRepository.useCancel(userCancelIn);

        long resCode = useCancelOut.getOutResCd();
        String resMsg = useCancelOut.getOutResMsg();

        if (resCode == ProcResCd.SUCCESS.getResCd()) {

            //성공 => PM_MPS_TRD 테이블
            long outExprPntAmt = useCancelOut.getOutExprPntAmt();//사용취소후 포인트 만료 금액(있으면 잔액조회+포인트만료 프로시저 태워야함)
            long outWaitMnyAmt = useCancelOut.getOutWaitMnyAmt();//사용취소후 발생된 대기머니 금액

            long outMnyAmt = useCancelOut.getOutMnyAmt();//사용취소된 머니 금액
            long outPntAmt = useCancelOut.getOutPntAmt();//사용취소된 포인트 금액

            long outMnyBlc = useCancelOut.getOutMnyBlc();//사용취소후 머니 잔액
            long outPntBlc = useCancelOut.getOutPntBlc();//사용취소후 포인트 잔액
            long outWaitMnyBlc = useCancelOut.getOutWaitMnyBlc();//사용취소후 대기머니 잔액

            log.info("** [사용취소]결과 ORG_TRD_NO:[{}] 사용취소된 머니:{} 사용취소된 포인트:{} 대기머니발생금액:{} 포인트만료금액:{}", orgTrdNo, outMnyAmt, outPntAmt, outWaitMnyAmt, outExprPntAmt);
            tradeRepository.save(Trade.builder()
                    .trdNo(trdNo) //거래번호
                    .trdDt(trdDt)
                    .trdTm(trdTm)
                    .trdDivCd(trdDivCd) //거래구분=사용취소
                    .svcCd(MpsApiCd.SVC_CD)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .mid(mId)
                    .blcUseOrd(orgTrade.getBlcUseOrd())//원거래 잔액사용순서 똑같이
                    .amtSign(+1)//사용취소 이므로 부호 (+)
                    .trdAmt(outMnyAmt + outPntAmt + outWaitMnyAmt) //취소 금액(프로시저 결과값 => 취소된 머니 + 취소된 포인트 + 대기머니 발생금액)
                    .mnyAmt(outMnyAmt) //취소 머니 금액
                    .pntAmt(outPntAmt) //취소 포인트 금액
                    .waitMnyAmt(outWaitMnyAmt) //취소 후, 발생 대기머니 금액
                    .mnyBlc(outMnyBlc) //취소 후, 머니 잔액
                    .pntBlc(outPntBlc) //취소 후, 포인트 잔액
                    .waitMnyBlc(outWaitMnyBlc) //취소 후, 대기머니 잔액
                    .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                    .mReqDtm(mReqDtm)
                    .mTrdNo(mTrdNo)
                    .mCustId(mCustId)
                    .mpsCustNo(mpsCustNo)
                    .trdSumry(trdSumry)
                    .orgTrdNo(orgTrdNo)//원거래번호
                    .orgTrdDt(orgTrdDt)//원거래일자
                    .csrcIssReqYn(orgTrade.getCsrcIssReqYn())//현금영수증 발급 관련 정보 원거래와 같도록
                    .csrcIssStatCd(orgTrade.getCsrcIssStatCd())//현금영수증 발급 관련 정보 원거래와 같도록
                    .cnclYn("Y")
                    .chrgMeanCd(orgTrade.getChrgMeanCd())//충전수단 코드 (원거래건 값인 ALL 그대로)
                    .createdId(SERVER_ID)
                    .createdIp(SERVER_IP)
                    .stlMId(orgTrade.getStlMId())
                    .storNm(storNm)
                    .storCd(storCd)
                    .mResrvField1(orgTrade.getMResrvField1())
                    .mResrvField2(orgTrade.getMResrvField2())
                    .mResrvField3(orgTrade.getMResrvField3())
                    .cardMngNo(cardUseCancelRequestDto.getCardMngNo())
                    .cnclTypeCd(cnclTypeCd)
                    .build());

            /* 원거래 업데이트 */
            orgTrade.orgTradeUpdate(outMnyAmt, outPntAmt, outWaitMnyAmt, (trdDt + trdTm));
            tradeRepository.save(orgTrade);

            if (outExprPntAmt > 0) {
                //1. 사용취소 프로시저 -> 2. 포인트 복원 -> 3. 만료 포인트 소멸처리 2->3 의 시간차가 너무 짧아서 오류 발생하여 Thread.sleep
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.info("사용 취소 로직 내 Thread sleep 오류 [{}] orgTrdNo:[{}] orgTrdDt:[{}] 확인 필요", e.getMessage(), cardUseCancelRequestDto.getOrgTrdNo(), cardUseCancelRequestDto.getOrgTrdDt());
                }
            }

            return WalletCancelResponseDto.builder()
                    .custNo(mpsCustNo)
                    .mTrdNo(mTrdNo)
                    .trdNo(trdNo)
                    .mnyBlc(String.valueOf(outMnyBlc))
                    .pntBlc(String.valueOf(outPntBlc))
                    .waitMnyBlc(String.valueOf(outWaitMnyBlc))
                    .expPntAmt(String.valueOf(outExprPntAmt))
                    .trdDt(trdDt)
                    .trdTm(trdTm)
                    .build();

        } else {
            log.info("선불금 사용취소 **실패** => 회원번호:[{}] 결과코드:[{}] 결과메세지:[{}]", mpsCustNo, resCode, resMsg);

            ErrorCode errorCode = ErrorCode.SERVER_ERROR_CODE;

            if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
                errorCode = ErrorCode.BALANCE_NOT_MATCHED;
            } else if (resCode == ProcResCd.REQ_AMT_NOT_MATCHED.getResCd()) {
                errorCode = ErrorCode.REQ_AMT_NOT_MATCHED;
            } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd()) {
                errorCode = ErrorCode.TRADE_AMT_ERROR;
            } else if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
                throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
            }
            TradeFailInsertDto trdFailDto = TradeFailInsertDto.builder()
                    .trdNo(trdNo)
                    .failDt(trdDt)
                    .failTm(trdTm)
                    .trdDivCd(trdDivCd)
                    .mid(mId)
                    .custNo(mpsCustNo)
                    .mCustId(mCustId)
                    .amtSign(+1)
                    .trdAmt(cancelMnyAmt + cancelPntAmt)//취소요청 금액
                    .mnyAmt(cancelMnyAmt)//취소요청 머니 금액
                    .pntAmt(cancelPntAmt)//취소요청 포인트 금액
                    .mnyBlc(Long.parseLong(cardUseCancelRequestDto.getMnyBlc()))//요청dto 내 머니 잔액
                    .pntBlc(Long.parseLong(cardUseCancelRequestDto.getPntBlc()))//요청dto 내 포인트 잔액
                    .reqDtm(trdDt + trdTm)//사용취소는 가맹점 요청 일시 없으므로 trdDt + trdTm 조합
                    .mTrdNo(mTrdNo)//가맹점 거래번호
                    .trdSumry(trdSumry)
                    .orgTrDt(orgTrdDt)
                    .orgTrNo(orgTrdNo)
                    .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())//충전수단 코드
                    .csrcIssReqYn(orgTrade.getCsrcIssReqYn())//현금영수증 발급 관련 정보 원거래와 같도록
                    .csrcIssStatCd(orgTrade.getCsrcIssStatCd())//현금영수증 발급 관련 정보 원거래와 같도록
                    .storNm(storNm)
                    .storCd(storCd)
                    .stlMId(orgTrade.getStlMId())
                    .errCd(String.valueOf(resCode))
                    .errMsg(resMsg)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .mResrvField1(cardUseCancelRequestDto.getMResrvField1())
                    .mResrvField2(cardUseCancelRequestDto.getMResrvField2())
                    .mResrvField3(cardUseCancelRequestDto.getMResrvField3())
                    .cardMngNo(cardUseCancelRequestDto.getCardMngNo())
                    .build();

            //실패 => PM_MPS_TRD_FAIL 테이블
            tradeFailService.insertTradeFail(trdFailDto);
            throw new RequestValidationException(errorCode);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public CreateParamCardUseApprovalResponseDto createParamUseApproval(CreateParamCardUseApprovalRequestDto createParamCardUseApprovalRequestDto) throws Exception {

        log.info("========== (MPS_CARD 승인 DTO SET 요청) ORN_ID: [{}], 거래승인번호: [{}], CARD_NO_ENC [{}], 승인금액: [{}] ==========", createParamCardUseApprovalRequestDto.getMTrdNo(), createParamCardUseApprovalRequestDto.getStorCd(), createParamCardUseApprovalRequestDto.getCardNoEnc(), createParamCardUseApprovalRequestDto.getTrdAmt());

        String ornId = createParamCardUseApprovalRequestDto.getStorCd();

        //카드번호 조회
        List<String> cardStatCdList = new ArrayList<>(Arrays.asList("H")); //카드분실신고 상태
        List<String> cardLastDivCdList = new ArrayList<>(Arrays.asList("2")); //카드탈회
        BpcCust tgtBpcCust = cardRepository.findByOrnIdAndCardStatCdNotInAndCardNoEncAndLastCardDivCdNotIn(ornId, cardStatCdList, createParamCardUseApprovalRequestDto.getCardNoEnc(), cardLastDivCdList);
        if (tgtBpcCust == null) {
            throw new RequestValidationException(ErrorCode.CARD_INFO_NOT_FOUND);
        }
        log.info("카드상태: [{}]", tgtBpcCust.getCardStatCd());

        String mpsCustNo = tgtBpcCust.getMpsCustNo();
        long mnyBlc = 0;
        long pntBlc = 0;
        long trdAmt = Long.parseLong(createParamCardUseApprovalRequestDto.getTrdAmt());

        //잔액조회
        try {
            CustomerWalletResponseDto custWallet = walletService.getCustWalletByCustNo(mpsCustNo);
            mnyBlc = custWallet.getMnyBlc();
            pntBlc = custWallet.getPntBlc();
        } catch (Exception e) {
            throw new RequestValidationException(ErrorCode.GET_BALANCE_ERROR);
        }

        MarketAddInfoDto marketAddInfo = commonService.getMarketAddInfoByCustNo(tgtBpcCust.getMpsCustNo());

        /* 암호화 */
        String encTrdAmt = "";
        String encMnyBlc = "";
        String encPntBlc = "";
        try {
            encTrdAmt = encrypt(String.valueOf(trdAmt), marketAddInfo.getEncKey());
            log.info("암호화된 거래금액: [{}]", encTrdAmt);
            encMnyBlc = encrypt(String.valueOf(mnyBlc), marketAddInfo.getEncKey());
            encPntBlc = encrypt(String.valueOf(pntBlc), marketAddInfo.getEncKey());
        } catch (Exception e) {
            log.error("========== (MPS_CARD) 암호화 오류 ==========");
            e.printStackTrace();
            throw new RequestValidationException(ErrorCode.ENCRYPT_ERROR);
        }

        /* make pktHash */
        StringBuilder builder = new StringBuilder().append(mpsCustNo + tgtBpcCust.getMid() + createParamCardUseApprovalRequestDto.getMTrdNo() + trdAmt);
        String pktHash = digestSHA256(builder + marketAddInfo.getPktHashKey());

        DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
        String decCardNo = databaseAESCryptoUtil.convertToEntityAttribute(createParamCardUseApprovalRequestDto.getCardNoEnc());

        String trdSumry = decCardNo.substring(decCardNo.length() - 3);
        if(ornId.equals(BpoOrnId.BC_CARD.getOrnId())){
            trdSumry = "BC(" + trdSumry + ")";
        }else if(ornId.equals(BpoOrnId.SAMSUNG_CARD.getOrnId())){
            trdSumry = "SCC(" + trdSumry + ")";
        }

        //카드승인용 DTO 세팅
        CreateParamCardUseApprovalResponseDto result = CreateParamCardUseApprovalResponseDto.builder()
                .custNo(mpsCustNo)
                .mTrdNo(createParamCardUseApprovalRequestDto.getMTrdNo())
                .reqDt(createParamCardUseApprovalRequestDto.getReqDt())
                .reqTm(createParamCardUseApprovalRequestDto.getReqTm())
                .trdAmt(encTrdAmt)
                .mnyBlc(encMnyBlc)
                .pntBlc(encPntBlc)
                .mid(tgtBpcCust.getMid())
                .pktHash(pktHash)
                .storNm(createParamCardUseApprovalRequestDto.getStorNm())
                .storCd(ornId)
                .mResrvField1(createParamCardUseApprovalRequestDto.getMResrvField1())
                .mResrvField2(createParamCardUseApprovalRequestDto.getMResrvField2())
                .mResrvField3(createParamCardUseApprovalRequestDto.getMResrvField3())
                .cardMngNo(tgtBpcCust.getCardMngNo())
                .trdSumry(trdSumry)
                .stlMId(useStlMid)
                .build();

        log.info("카드 승인 DTO응답: [{}]", result);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public CreateParamCardUseCancelApprovalResponseDto createParamUseCancelApproval(CreateParamCardUseCancelApprovalRequestDto createParamCardUseCancelApprovalRequestDto) throws Exception {

        log.info("createParamCardUseCancelApprovalRequestDto: [{}]", createParamCardUseCancelApprovalRequestDto);

        /* req_no 채번 */
        String reqNo = sequenceService.generateBpcApprReqSeq01();

        log.info("========== (MPS_CARD 승인취소 DTO SET 요청) 요청번호: [{}], ORN_ID: [{}], 거래승인번호: [{}], CARD_NO_ENC [{}], 승인금액: [{}] ==========", reqNo, createParamCardUseCancelApprovalRequestDto.getMTrdNo(), createParamCardUseCancelApprovalRequestDto.getStorCd(), createParamCardUseCancelApprovalRequestDto.getCardNoEnc(), createParamCardUseCancelApprovalRequestDto.getTrdAmt());

        //카드번호 조회
        BpcCust tgtBpcCust = cardRepository.findByOrnIdAndCardNoEnc(createParamCardUseCancelApprovalRequestDto.getStorCd(), createParamCardUseCancelApprovalRequestDto.getCardNoEnc());
        if (tgtBpcCust == null) {
            throw new RequestValidationException(ErrorCode.CARD_INFO_NOT_FOUND);
        }
        log.info("카드상태: [{}]", tgtBpcCust.getCardStatCd());

        String mpsCustNo = tgtBpcCust.getMpsCustNo();
        long mnyBlc = 0;
        long pntBlc = 0;
        long trdAmt = Long.parseLong(createParamCardUseCancelApprovalRequestDto.getTrdAmt()); //취소요청금액

        //잔액조회
        try {
            CustomerWalletResponseDto custWallet = walletService.getCustWalletByCustNo(mpsCustNo);
            mnyBlc = custWallet.getMnyBlc();
            pntBlc = custWallet.getPntBlc();
        } catch (Exception e) {
            throw new RequestValidationException(ErrorCode.GET_BALANCE_ERROR);
        }

        //원거래조회
        List<Trade> orgTrade = tradeRepository.findByTrdDivCdAndMTrdNoAndStorCdAndMpsCustNo(TrdDivCd.COMMON_USE.getTrdDivCd(), createParamCardUseCancelApprovalRequestDto.getMTrdNo(), createParamCardUseCancelApprovalRequestDto.getStorCd(), mpsCustNo, tgtBpcCust.getCardMngNo());
        if(orgTrade.size() > 1 || orgTrade.size() == 0){
            throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_NOT_FOUND);
        }
        log.info("원거래번호: [{}]", orgTrade.get(0).getTrdNo());

        MarketAddInfoDto marketAddInfo = commonService.getMarketAddInfoByCustNo(tgtBpcCust.getMpsCustNo());

        long orgUseMnyAmt = orgTrade.get(0).getMnyAmt();
        long orgUsePntAmt = orgTrade.get(0).getPntAmt();
        long cancelableAmt = orgTrade.get(0).getTrdAmt() - orgTrade.get(0).getCnclTrdAmt(); //현재취소가능 금액

        //취소머니금액, 취소포인트금액
        long cancelMnyAmt = orgUseMnyAmt;
        long cancelPntAmt = orgUsePntAmt;

        log.info("현재 취소 가능금액: [{}]", cancelableAmt);
        if(cancelableAmt <= 0){
            throw new RequestValidationException(ErrorCode.NOT_VALID_TRD_DIV_CD);
        }
        if(trdAmt > cancelableAmt){
            throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
        }

        /* 취소거래금액 (매입파일로 들어오는 경우 부분취소 거래가 있을 수 있음) */
        if(trdAmt != orgTrade.get(0).getTrdAmt()){
            log.info("========== (MPS_CARD 승인취소 - 부분취소 인입됨) ==========");
            //취소요청금액이랑 원거래의 TRD_AMT 랑 다를경우 부분취소. 취소머니금액 세팅
            long orgCnclMnyAmt = orgTrade.get(0).getCnclMnyAmt(); //현재까지 취소된 머니금액
            long orgCnclPntAmt = orgTrade.get(0).getCnclPntAmt(); //현재까지 취소된 포인트금액


            long remainMoneyAmt = orgUseMnyAmt - orgCnclMnyAmt;
            long remainPointAmt = orgUsePntAmt - orgCnclPntAmt;

            cancelMnyAmt= Math.min(trdAmt, remainMoneyAmt);
            cancelPntAmt = trdAmt - cancelMnyAmt;

            log.info(
                    "취소요청금액: [{}], 최종 머니취소금액: [{}], 최종 포인트취소금액: [{}], 취소가능잔액(머니): [{}], 취소가능잔액(포인트): [{}]",
                    trdAmt, cancelMnyAmt, cancelPntAmt, remainMoneyAmt, remainPointAmt
            );

            if (cancelPntAmt > remainPointAmt) {
                throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
            }
        }

        /* 암호화 */
        String encTrdAmt = "";
        String encMnyBlc = "";
        String encPntBlc = "";
        String encCnclMnyAmt = "";
        String encCnclPntAmt = "";
        try {
            encTrdAmt = encrypt(String.valueOf(trdAmt), marketAddInfo.getEncKey());
            log.info("암호화된 거래금액: [{}]", encTrdAmt);
            encMnyBlc = encrypt(String.valueOf(mnyBlc), marketAddInfo.getEncKey());
            encPntBlc = encrypt(String.valueOf(pntBlc), marketAddInfo.getEncKey());
            encCnclMnyAmt = encrypt(String.valueOf(cancelMnyAmt), marketAddInfo.getEncKey());
            encCnclPntAmt = encrypt(String.valueOf(cancelPntAmt), marketAddInfo.getEncKey());
        } catch (Exception e) {
            log.error("========== (MPS_CARD) 암호화 오류 ==========");
            e.printStackTrace();
            throw new RequestValidationException(ErrorCode.ENCRYPT_ERROR);
        }

        StringBuilder builder = new StringBuilder().append(mpsCustNo + tgtBpcCust.getMid() + createParamCardUseCancelApprovalRequestDto.getMTrdNo() + orgTrade.get(0).getTrdNo() + orgTrade.get(0).getTrdDt());
        String pktHash = digestSHA256(builder + marketAddInfo.getPktHashKey());

        DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
        String decCardNo = databaseAESCryptoUtil.convertToEntityAttribute(createParamCardUseCancelApprovalRequestDto.getCardNoEnc());
        String trdSumry = "BC(" + decCardNo.substring(decCardNo.length() - 3) + ")";

        CreateParamCardUseCancelApprovalResponseDto result = CreateParamCardUseCancelApprovalResponseDto.builder()
                .reqNo(reqNo)
                .custNo(mpsCustNo)
                .mTrdNo(createParamCardUseCancelApprovalRequestDto.getMTrdNo())
                .reqDt(createParamCardUseCancelApprovalRequestDto.getReqDt())
                .reqTm(createParamCardUseCancelApprovalRequestDto.getReqTm())
                .trdAmt(encTrdAmt)
                .cnclMnyAmt(encCnclMnyAmt)
                .cnclPntAmt(encCnclPntAmt)
                .mnyBlc(encMnyBlc)
                .pntBlc(encPntBlc)
                .orgTrdDt(orgTrade.get(0).getTrdDt())
                .orgTrdNo(orgTrade.get(0).getTrdNo())
                .pktHash(pktHash)
                .storNm(orgTrade.get(0).getStorNm())
                .storCd(orgTrade.get(0).getStorCd())
                .mResrvField1(orgTrade.get(0).getMResrvField1())
                .mResrvField2(orgTrade.get(0).getMResrvField2())
                .mResrvField3(orgTrade.get(0).getMResrvField3())
                .cardMngNo(tgtBpcCust.getCardMngNo())
                .trdSumry(trdSumry)
                .build();
        log.info("result: [{}]", result);
        return result;
    }
}