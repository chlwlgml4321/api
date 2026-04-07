package kr.co.hectofinancial.mps.api.v1.trade.service.wallet;

import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountRequestDto;
import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountResponseDto;
import kr.co.hectofinancial.mps.api.v1.authentication.service.AuthenticationService;
import kr.co.hectofinancial.mps.api.v1.authentication.service.BillKeyService;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarket;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.market.repository.MpsMarketRepository;
import kr.co.hectofinancial.mps.api.v1.notification.dto.PyNtcSendInsertRequestDto;
import kr.co.hectofinancial.mps.api.v1.notification.service.NotiService;
import kr.co.hectofinancial.mps.api.v1.trade.domain.CustWllt;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeFailInsertDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeFindByTrDivCdCountRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustChrgMeanDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.WithdrawalMoneyRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.WithdrawalMoneyResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.*;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlc.GetBlcIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlc.GetBlcOut;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.GetMWAmt.GetMWAmtIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.GetMWAmt.GetMWAmtOut;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Use.UseIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Use.UseOut;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseCancel.UseCancelIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseCancel.UseCancelOut;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseEach.UseEachIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseEach.UseEachOut;
import kr.co.hectofinancial.mps.api.v1.trade.repository.CustWlltRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.PayMnyRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.PayPntRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepository;
import kr.co.hectofinancial.mps.api.v1.trade.service.TradeFailService;
import kr.co.hectofinancial.mps.api.v1.trade.service.TradeService;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.ApprovalService;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto.AutoChargeAvailabilityValidService;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto.CustChrgMeanService;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.constant.*;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kr.co.hectofinancial.mps.global.util.CipherSha256Util.digestSHA256;



@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    @Value("${spring.profiles.active}")
    private String profiles;
    private final TradeRepository tradeRepository;
    private final TradeService tradeService;
    private final SequenceService sequenceService;
    private final TradeFailService tradeFailService;
    private final CommonService commonService;
    private final CustWlltRepository custWlltRepository;
    private final NotiService notiService;
    private final String SERVER_IP = ServerInfoConfig.HOST_IP;
    private final String SERVER_ID = ServerInfoConfig.HOST_NAME;
    private final AuthenticationService authenticationService;
    private final BillKeyService billKeyService;
    private final MpsMarketRepository mpsMarketRepository;
    private final ApprovalService approvalService;
    private final PayMnyRepository payMnyRepository;
    private final PayPntRepository payPntRepository;
    private final CustChrgMeanService custChrgMeanService;
    private final AutoChargeAvailabilityValidService autoChargeAvailabilityValidService;

    @Transactional(rollbackFor = Exception.class)
    public WalletUseResponseDto useWallet(WalletUseRequestDto walletUseRequestDto) {

        //잔액사용순서 default P
        String blcUseOrd = "P";
        if (StringUtils.isNotEmpty(walletUseRequestDto.getBlcUseOrd())) {
            blcUseOrd = walletUseRequestDto.getBlcUseOrd().toUpperCase();
        }

        //잔액 사용순서 검증
        if (!blcUseOrd.equals("P") && !blcUseOrd.equals("M")) {
            throw new RequestValidationException(ErrorCode.INVALID_BLC_USE_ORD);
        }

        //현금영수증 default N
        String csrcIssReqYn = "N";
        if (StringUtils.isNotEmpty(walletUseRequestDto.getCsrcIssReqYn())) {
            csrcIssReqYn = walletUseRequestDto.getCsrcIssReqYn().toUpperCase();

            if(csrcIssReqYn.equals("Y")){
                //todo Y로 들어오면 해당상점에 현금영수증 서비스가 등록되어있는지 확인해야함(open_info)
                log.info("여기탐");
                commonService.validateMarketOpenInfoByMIdAndPrdtCd(walletUseRequestDto.getCustomerDto().getMid(), "CSRC", "CR");
            }
        }

        //현금영수증 발행 여부 검증
        if (!csrcIssReqYn.equals("Y") && !csrcIssReqYn.equals("N")) {
            throw new RequestValidationException(ErrorCode.INVALID_CSRC_ISS_REQ_YN);
        }

        //회원 추출
        CustomerDto customerDto = walletUseRequestDto.getCustomerDto();

        //변수 선언
        String trdNo = sequenceService.generateTradeSeq01();
        String mTrdNo = walletUseRequestDto.getMTrdNo();

        CustomDateTimeUtil today = new CustomDateTimeUtil();
        String trdDt = today.getDate();
        String trdTm = today.getTime();

        String mpsCustNo = customerDto.getMpsCustNo();
        String mId = customerDto.getMid();
        String mCustId = customerDto.getMCustId();
        String trdDivCd = TrdDivCd.COMMON_USE.getTrdDivCd(); //고정

        long trdAmt = Long.parseLong(walletUseRequestDto.getTrdAmt());
        long mnyBlc = Long.parseLong(walletUseRequestDto.getMnyBlc());
        long pntBlc = Long.parseLong(walletUseRequestDto.getPntBlc());
        long inBlc = mnyBlc + pntBlc;

        String storCd = StringUtils.isEmpty(walletUseRequestDto.getStorCd()) ? "N" : walletUseRequestDto.getStorCd();
        String storNm = StringUtils.isEmpty(walletUseRequestDto.getStorNm()) ? "N" : URLDecoder.decode(walletUseRequestDto.getStorNm());

        String stlMId = walletUseRequestDto.getCustomerDto().getMid();
        if (StringUtils.isNotEmpty(walletUseRequestDto.getStlMId())) {
            stlMId = walletUseRequestDto.getStlMId();
        }
        commonService.checkValidStlMid(stlMId, mId);

        //금액 음수 검증
        if (trdAmt < 0 || mnyBlc < 0 || pntBlc < 0) {
            throw new RequestValidationException(ErrorCode.AMT_CANNOT_BE_NEGATIVE);
        }
        String mReqDtm = trdDt + trdTm;
        if (StringUtils.isNotEmpty(walletUseRequestDto.getReqDt()) && StringUtils.isNotEmpty(walletUseRequestDto.getReqTm())) {
            mReqDtm = walletUseRequestDto.getReqDt() + walletUseRequestDto.getReqTm();
        }

        /* 핀번호, 빌키 검증 */
        String mid = customerDto.getMid();

        MpsMarket mpsMarket = mpsMarketRepository.findMpsMarketByMid(mid).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));

        if ("N".equalsIgnoreCase(mpsMarket.getPinVrifyYn())) {
            //검증안하는 가맹점
            log.info("###### CheckPin [End] => mId:[{}] pinVrifyYn is N", mid);
        } else {
            String pinNo = walletUseRequestDto.getPinNo();
            boolean isBillKeyUsed = "Y".equals(mpsMarket.getBillKeyUseYn());
            boolean hasBillKey = !CommonUtil.nullTrim(customerDto.getBillKeyEnc()).isEmpty();
            boolean isPinFormat = StringUtils.isNotBlank(pinNo) && pinNo.length() == 6;

            ChkPinErrorCountResponseDto chkPinErrorCountResponseDto = null;
            ChkPinErrorCountRequestDto chkPinErrorCountRequestDto = ChkPinErrorCountRequestDto.builder().pin(pinNo).trdNo(trdNo).customerDto(customerDto).mpsMarket(mpsMarket).build();

            if (isBillKeyUsed && hasBillKey && !isPinFormat) {
                chkPinErrorCountResponseDto = billKeyService.isCorrectBillkey(chkPinErrorCountRequestDto);
            } else {
                chkPinErrorCountResponseDto = authenticationService.isCorrectWhiteLabelPin(chkPinErrorCountRequestDto);
            }

            /* 거래실패 SET */
            if (!PinVerifyResult.SUCCESS.equals(chkPinErrorCountResponseDto.getPinVerifyResult())) {

                TradeFailInsertDto trdFailDto = TradeFailInsertDto.builder()
                        .blcUseOrd(blcUseOrd)//잔액사용순서
                        .trdNo(trdNo)//거래번호
                        .trdDivCd(trdDivCd)//거래구분코드
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
                        .csrcIssReqYn(csrcIssReqYn)
                        .csrcIssStatCd("N")
                        .failDt(trdDt)//거래일자
                        .failTm(trdTm)//거래시간
                        .stlMId(stlMId)
                        .errCd(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorCode())
                        .errMsg(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorMessage() + chkPinErrorCountResponseDto.getPinVerifyResultMsgForTrdFail())
                        .storCd(storCd)
                        .storNm(storNm)
                        .prdtCd(MpsPrdtCd.use.getPrdtCd())
                        .build();
                tradeFailService.insertTradeFail(trdFailDto);

                throw new RequestValidationException(ErrorCode.PIN_NOT_MATCHED, chkPinErrorCountResponseDto.getPinVerifyResultMsg());
            }
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
            if (outMnyAmt == 0) csrcIssReqYn = "N";

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
                    .csrcIssReqYn(csrcIssReqYn)//현금영수증 발행 Yn
                    .csrcIssStatCd("N")//현금영수증 발행 상태 N
                    .stlMId(stlMId)//정산 상점 아이디
                    .storCd(storCd)
                    .storNm(storNm)
                    .mResrvField1(walletUseRequestDto.getMResrvField1())
                    .mResrvField2(walletUseRequestDto.getMResrvField2())
                    .mResrvField3(walletUseRequestDto.getMResrvField3())
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
                            .stlMid(stlMId)
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

            return WalletUseResponseDto.builder()
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
                    .csrcIssReqYn(csrcIssReqYn)
                    .csrcIssStatCd("N")
                    .failDt(trdDt)//거래일자
                    .failTm(trdTm)//거래시간
                    .stlMId(stlMId)
                    .errCd(String.valueOf(resCode))
                    .errMsg(resMsg)
                    .storCd(storCd)
                    .storNm(storNm)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .mResrvField1(walletUseRequestDto.getMResrvField1())
                    .mResrvField2(walletUseRequestDto.getMResrvField2())
                    .mResrvField3(walletUseRequestDto.getMResrvField3())
                    .build();
            tradeFailService.insertTradeFail(trdFailDto);

            throw new RequestValidationException(errorCode);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public WalletCancelResponseDto cancelWallet(WalletCancelRequestDto walletCancelRequestDto) {

        CustomDateTimeUtil today = new CustomDateTimeUtil();
        String trdDt = today.getDate();
        String trdTm = today.getTime();

        //원거래 조회
        Trade orgTrade = tradeRepository.findTradeByMpsCustNoAndTrdNoAndTrdDt(walletCancelRequestDto.getCustNo(), walletCancelRequestDto.getOrgTrdNo(), walletCancelRequestDto.getOrgTrdDt())
                .orElseThrow(() -> new RequestValidationException(ErrorCode.ORG_TRADE_INFO_NOT_FOUND));

        if (CommonUtil.nullTrim(walletCancelRequestDto.getCnclMnyAmt()).equals("") || CommonUtil.nullTrim(walletCancelRequestDto.getCnclPntAmt()).equals("")) {
            throw new RequestValidationException(ErrorCode.USE_CANCEL_AMT_ERROR);
        }

        long cancelMnyAmt = Long.parseLong(walletCancelRequestDto.getCnclMnyAmt()); //취소 요청 머니 금액
        long cancelPntAmt = Long.parseLong(walletCancelRequestDto.getCnclPntAmt()); //취소 요청 포인트 금액

        /* 카드승인거래 취소 불가 */
        if (orgTrade.getStorCd().equals("BCC")) {
            throw new RequestValidationException(ErrorCode.TRADE_CANCELED_FAIL);
        }
        //원거래건의 거래구분코드 "사용" 검증
        if (!TrdDivCd.COMMON_USE.getTrdDivCd().equals(orgTrade.getTrdDivCd())) {
            log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] TRD_DIV_CD:[{}] 원거래건의 거래구분 코드가 [사용]이 아님", walletCancelRequestDto.getOrgTrdNo(), walletCancelRequestDto.getOrgTrdDt(), orgTrade.getTrdDivCd());
            throw new RequestValidationException(ErrorCode.NOT_VALID_TRD_DIV_CD);
        }
        //이전 취소건 검증
        if (orgTrade.getTrdAmt() == orgTrade.getCnclTrdAmt()) {
            log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 기취소건에 대한 취소 요청 인입", walletCancelRequestDto.getOrgTrdNo(), walletCancelRequestDto.getOrgTrdDt());
            throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_CANCELED);
        }

        if ("1".equals(orgTrade.getCnclTypeCd())) {

            //BPO카드 재발급 사용건 당일취소만 가능하도록
            if (!orgTrade.getTrdDt().equals(trdDt)) {
                throw new RequestValidationException(ErrorCode.TRADE_CANCELED_FAIL, "카드 재발급 취소는 당일취소만 가능해요.");
            }

            if (orgTrade.getCnclTrdAmt() > 0) { //BPO 재발급 비용 부분취소안됨
                throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_CANCELED);
            }

        } else {
            if (orgTrade.getTrdAmt() < (orgTrade.getCnclTrdAmt() + cancelMnyAmt + cancelPntAmt)) {
                log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 취소요청 금액이 원거래금액보다 큼(전체)", walletCancelRequestDto.getOrgTrdNo(), walletCancelRequestDto.getOrgTrdDt());
                throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
            }
            if (orgTrade.getMnyAmt() < (orgTrade.getCnclMnyAmt() + cancelMnyAmt)) {
                log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 취소요청 금액이 원거래금액보다 큼(머니)", walletCancelRequestDto.getOrgTrdNo(), walletCancelRequestDto.getOrgTrdDt());
                throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
            }
            if (orgTrade.getPntAmt() < (orgTrade.getCnclPntAmt() + cancelPntAmt)) {
                log.info("ORG_TRD_NO:[{}] ORG_TRD_DT:[{}] 취소요청 금액이 원거래금액보다 큼(포인트)", walletCancelRequestDto.getOrgTrdNo(), walletCancelRequestDto.getOrgTrdDt());
                throw new RequestValidationException(ErrorCode.CNCL_TRADE_AMT_NOT_MATCH);
            }
        }

        //회원추출
        CustomerDto customerDto = walletCancelRequestDto.getCustomerDto();

        //변수 선언
        String trdNo = sequenceService.generateTradeSeq01();

        String mTrdNo = walletCancelRequestDto.getMTrdNo();
        String mpsCustNo = customerDto.getMpsCustNo();
        String mCustId = customerDto.getMCustId();
        String mId = customerDto.getMid();
        String trdDivCd = TrdDivCd.USE_COMMON_CANCEL.getTrdDivCd();
        String trdSumry = walletCancelRequestDto.getTrdSumry();

        long inBlc = Long.parseLong(walletCancelRequestDto.getMnyBlc()) + Long.parseLong(walletCancelRequestDto.getPntBlc());

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
                    .mReqDtm(trdDt + trdTm)
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
                    .mResrvField1(walletCancelRequestDto.getMResrvField1())
                    .mResrvField2(walletCancelRequestDto.getMResrvField2())
                    .mResrvField3(walletCancelRequestDto.getMResrvField3())
                    .build());

            /* 원거래 업데이트 */
            orgTrade.orgTradeUpdate(outMnyAmt, outPntAmt, outWaitMnyAmt, (trdDt + trdTm));
            tradeRepository.save(orgTrade);

            if (outExprPntAmt > 0) {
                //1. 사용취소 프로시저 -> 2. 포인트 복원 -> 3. 만료 포인트 소멸처리 2->3 의 시간차가 너무 짧아서 오류 발생하여 Thread.sleep
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.info("사용 취소 로직 내 Thread sleep 오류 [{}] orgTrdNo:[{}] orgTrdDt:[{}] 확인 필요", e.getMessage(), walletCancelRequestDto.getOrgTrdNo(), walletCancelRequestDto.getOrgTrdDt());
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
                    .mnyBlc(Long.parseLong(walletCancelRequestDto.getMnyBlc()))//요청dto 내 머니 잔액
                    .pntBlc(Long.parseLong(walletCancelRequestDto.getPntBlc()))//요청dto 내 포인트 잔액
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
                    .mResrvField1(walletCancelRequestDto.getMResrvField1())
                    .mResrvField2(walletCancelRequestDto.getMResrvField2())
                    .mResrvField3(walletCancelRequestDto.getMResrvField3())
                    .build();

            //실패 => PM_MPS_TRD_FAIL 테이블
            tradeFailService.insertTradeFail(trdFailDto);

            throw new RequestValidationException(errorCode);
        }
    }

    /* 회원 잔액조회(만료x) */
    @Transactional(readOnly = true)
    public CustomerWalletResponseDto getCustWalletByCustNo(String custNo) {

        /* 일자 시각 SET */
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();

        Long willExpPntAmount = 0L;
        Long expMnyAmount = 0L;
        Long chrgPssbAmt = 0L;

        if (custNo == null) {
            throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        /* 회원조회 */
        CustomerDto customer = commonService.getCustomerByCustNo(custNo);

        /* 잔액조회 프로시저 호출 */
        GetBlcIn getBlcIn = GetBlcIn.builder()
                .inMpsCustNo(custNo)
                .inChrgLmtAmt(customer.getChrgLmtAmlt())
                .build();
        GetBlcOut custWlltBlc = custWlltRepository.getBlc(getBlcIn);
        long resCode = (long) custWlltBlc.getOutResCd();
        String resMsg = custWlltBlc.getOutResMsg();

        if (resCode == ProcResCd.SUCCESS.getResCd()) {
            /* 충전가능금액 머니*/
            Number mnyBlc = (custWlltBlc.getOutMnyBlc() == null ? 0 : custWlltBlc.getOutMnyBlc());
            chrgPssbAmt = (Long) custWlltBlc.getOutChrgPsbAmt();
            log.info("선불회원번호: [{}], 충전한도금: [{}] 현재충전금: [{}] 잔여충전한도금: [{}]", customer.getMpsCustNo(), customer.getChrgLmtAmlt(), mnyBlc, chrgPssbAmt);

            /* 소멸예정머니(2M) 머니유효기간 10년*/
            //TODO 추후 개발

//            /* 소멸예정포인트(3D) */
//            String expDt = DateTimeUtil.addDate(curDt, 3);
//            willExpPntAmount = payPntRepository.getExpPntTotal(custNo, customDateTimeUtil.getDate(), expDt);
//            willExpPntAmount = (willExpPntAmount == null ? 0 : willExpPntAmount);

        } else {
            log.info("잔액조회 실패 => 회원번호 :{} 에러메세지: {}", custNo, resMsg);
            throw new RequestValidationException(ErrorCode.GET_BALANCE_ERROR);
        }

        CustomerWalletResponseDto customerWalletResponseDto = CustomerWalletResponseDto.builder()
                .custNo(custNo)
                .custDivCd(customer.getCustDivCd())
                .mnyBlc((Long) custWlltBlc.getOutMnyBlc())
                .pntBlc((Long) custWlltBlc.getOutPntBlc())
                .waitMnyBlc((Long) custWlltBlc.getOutWaitMnyBlc())
                .expMnyBlc(expMnyAmount)
                .chrgblAmt(chrgPssbAmt)
                .build();
        return customerWalletResponseDto;
    }
    @Transactional(readOnly = true)
    public CustomerWalletResponseDto getCustWalletByCustNoNew(String custNo, CustomerDto customerDto) {

        if (customerDto == null) {
            customerDto = commonService.getCustomerByCustNo(custNo);
        }

        /* 잔액조회 프로시저 호출 */
        custNo = customerDto.getMpsCustNo();

        GetBlcIn getBlcIn = GetBlcIn.builder()
                .inMpsCustNo(custNo)
                .inChrgLmtAmt(customerDto.getChrgLmtAmlt())
                .build();
        GetBlcOut custWlltBlc = custWlltRepository.getBlc(getBlcIn);

        long resCode = (long) custWlltBlc.getOutResCd();
        String resMsg = custWlltBlc.getOutResMsg();

        if (resCode != ProcResCd.SUCCESS.getResCd()) {
            log.info("잔액조회 실패 => 회원번호 :{} 에러메세지: {}", custNo, resMsg);
            throw new RequestValidationException(ErrorCode.GET_BALANCE_ERROR);
        }
        Long outMnyBlc = (Long) custWlltBlc.getOutMnyBlc();
        Long outChrgPsbAmt = (Long) custWlltBlc.getOutChrgPsbAmt();

        log.info("선불회원번호: [{}], 충전한도금: [{}] 현재충전금: [{}] 잔여충전한도금: [{}]", custNo, customerDto.getChrgLmtAmlt(), outMnyBlc, outChrgPsbAmt);

        CustomerWalletResponseDto customerWalletResponseDto = CustomerWalletResponseDto.builder()
                .custNo(custNo)
                .custDivCd(customerDto.getCustDivCd())
                .mnyBlc(outMnyBlc)
                .pntBlc((Long) custWlltBlc.getOutPntBlc())
                .waitMnyBlc((Long) custWlltBlc.getOutWaitMnyBlc())
                .expMnyBlc(0L)
                .chrgblAmt(outChrgPsbAmt)
                .build();
        return customerWalletResponseDto;
    }

    /* 잔액조회(만료x) -> 사용자조회용 */
    @Transactional(readOnly = true)
    public WalletBalanceResponseDto getWalletBalanceByCustNo(String custNo) {
        CustomerWalletResponseDto customerWalletResponseDto = getCustWalletByCustNo(custNo);

        return WalletBalanceResponseDto.builder()
                .custNo(custNo)
                .custDivCd(customerWalletResponseDto.getCustDivCd())
                .chrgblAmt(String.valueOf(customerWalletResponseDto.getChrgblAmt()))
                .mnyBlc(String.valueOf(customerWalletResponseDto.getMnyBlc()))
                .pntBlc(String.valueOf(customerWalletResponseDto.getPntBlc()))
                .waitMnyBlc(String.valueOf(customerWalletResponseDto.getWaitMnyBlc()))
                .expMnyBlc(String.valueOf(customerWalletResponseDto.getExpMnyBlc()))
                .build();
    }

    /* 출금가능 머니조회 */
    @Transactional(rollbackFor = Exception.class)
    public WithdrawalMoneyResponseDto getWithdrawalMoneyAmt(WithdrawalMoneyRequestDto withdrawalMoneyRequestDto) {

        /* 회원 조회 */
        CustomerDto customer = commonService.getCustomerByCustNo(withdrawalMoneyRequestDto.getCustNo());
        if (customer.getCustDivCd().equals(CustDivCd.ANONYMOUS.getCustDivCd())) {
            throw new RequestValidationException(ErrorCode.CUSTOMER_UNINSCRIBED);
        }

        /* 출금가능 머니조회 프로시저 호출 */
        GetMWAmtIn getMWAmtIn = GetMWAmtIn.builder()
                .inMpsCustNo(customer.getMpsCustNo())
                .build();
        GetMWAmtOut getMWAmtOut = custWlltRepository.mwAmt(getMWAmtIn);
        long resCode = (long) getMWAmtOut.getOutResCd();
        log.info("선불회원번호: [{}], 응답코드: [{}], 출금가능금액: [{}]", customer.getMpsCustNo(), resCode, getMWAmtOut.getOutMwAmt());

        if (resCode == ProcResCd.SUCCESS.getResCd()) {
            CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();

            /* 출금횟수 */
            TradeFindByTrDivCdCountRequestDto tradeFindByTrDivCdCountRequestDto = new TradeFindByTrDivCdCountRequestDto();
            tradeFindByTrDivCdCountRequestDto.setCustNo(customer.getMpsCustNo());
            tradeFindByTrDivCdCountRequestDto.setDivCd(TrdDivCd.MONEY_WITHDRAW.getTrdDivCd());
            tradeFindByTrDivCdCountRequestDto.setTargetMonth(customDateTimeUtil.getDate().substring(0, 6));
            Long mwCount = tradeService.findByTrDivCdCount(tradeFindByTrDivCdCountRequestDto);

            WithdrawalMoneyResponseDto withdrawalMoneyResponseDto = WithdrawalMoneyResponseDto.builder()
                    .custNo(withdrawalMoneyRequestDto.getCustNo())
                    .wdMnyAmt(String.valueOf(getMWAmtOut.getOutMwAmt()))
                    .mwCount(String.valueOf(mwCount))
                    .build();
            return withdrawalMoneyResponseDto;
        } else {
            throw new RequestValidationException(ErrorCode.GET_WITHDRAWAL_AMT_ERROR);
        }
    }

    /* 회원 지갑 상세 조회 */
    @Transactional(readOnly = true)
    public CustomerWalletDtlResponseDto getWalletBalanceDtl(String custNo) {

        CustomerWalletDtlResponseDto customerWalletDtlResponseDto = new CustomerWalletDtlResponseDto();

        WalletBalanceResponseDto walletBalanceResponseDto = getWalletBalanceByCustNo(custNo);
        customerWalletDtlResponseDto.setCustNo(custNo);
        customerWalletDtlResponseDto.setCustDivCd(walletBalanceResponseDto.getCustDivCd());
        customerWalletDtlResponseDto.setChrgblAmt(walletBalanceResponseDto.getChrgblAmt());
        customerWalletDtlResponseDto.setMnyBlc(walletBalanceResponseDto.getMnyBlc());
        customerWalletDtlResponseDto.setPntBlc(walletBalanceResponseDto.getPntBlc());
        customerWalletDtlResponseDto.setWaitMnyBlc(walletBalanceResponseDto.getWaitMnyBlc());

        List<CustWllt> tgtCustWllt = custWlltRepository.findByMpsCustNo(custNo);

        Map<String, List<Map<String, String>>> resultListMap = tgtCustWllt.stream()
                .filter(item -> item.getBlc() > 0)
                .collect(Collectors.groupingBy(
                        CustWllt::getBlcDivCd,
                        Collectors.mapping(item -> {
                            Map<String, String> detailMap = new HashMap<>();
                            detailMap.put(item.getChrgMeanCd(), String.valueOf(item.getBlc()));
                            return detailMap;
                        }, Collectors.toList())
                ));

        customerWalletDtlResponseDto.setData(resultListMap);
        return customerWalletDtlResponseDto;
    }

    /* 머니, 포인트 각각 사용 */
    @Transactional(rollbackFor = Exception.class)
    public WalletUseEachResponseDto useEachWallet(WalletUseEachRequestDto walletUseEachRequestDto) {

        /* 일자 시각 SET */
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();

        String reqDtm = curDt + curTm;
        if (StringUtils.isNoneBlank(walletUseEachRequestDto.getReqDt()) && StringUtils.isNoneBlank(walletUseEachRequestDto.getReqTm())) {
            reqDtm = walletUseEachRequestDto.getReqDt() + walletUseEachRequestDto.getReqTm();
        }

        //현금영수증 default N
        String csrcIssReqYn = "N";
        if (StringUtils.isNoneBlank(walletUseEachRequestDto.getCsrcIssReqYn())) {
            csrcIssReqYn = walletUseEachRequestDto.getCsrcIssReqYn().toUpperCase();
        }

        //현금영수증 발행 여부 검증
        if (!csrcIssReqYn.equals("Y") && !csrcIssReqYn.equals("N")) {
            throw new RequestValidationException(ErrorCode.INVALID_CSRC_ISS_REQ_YN);
        }

        //회원 추출
        CustomerDto customerDto = walletUseEachRequestDto.getCustomerDto();
        String trdNo = sequenceService.generateTradeSeq01();
        String mTrdNo = walletUseEachRequestDto.getMTrdNo();

        String mpsCustNo = customerDto.getMpsCustNo();
        String mid = customerDto.getMid();
        String mCustId = customerDto.getMCustId();

        long mnyAmt = Long.parseLong(walletUseEachRequestDto.getMnyAmt());
        long pntAmt = Long.parseLong(walletUseEachRequestDto.getPntAmt());
        long mnyBlc = Long.parseLong(walletUseEachRequestDto.getMnyBlc());
        long pntBlc = Long.parseLong(walletUseEachRequestDto.getPntBlc());
        long inBlc = mnyBlc + pntBlc;
        boolean chkPin = true;

        String storCd = StringUtils.isEmpty(walletUseEachRequestDto.getStorCd()) ? "N" : walletUseEachRequestDto.getStorCd();
        String storNm = StringUtils.isEmpty(walletUseEachRequestDto.getStorNm()) ? "N" : URLDecoder.decode(walletUseEachRequestDto.getStorNm());

        String stlMId = walletUseEachRequestDto.getCustomerDto().getMid();
        if (StringUtils.isNotEmpty(walletUseEachRequestDto.getStlMId())) {
            stlMId = walletUseEachRequestDto.getStlMId();
        }

        commonService.checkValidStlMid(stlMId, mid);

        //금액 음수 검증
        if (mnyAmt < 0 || pntAmt < 0 || mnyBlc < 0 || pntBlc < 0) {
            throw new RequestValidationException(ErrorCode.AMT_CANNOT_BE_NEGATIVE);
        }
        if (mnyAmt == 0 && pntAmt > 0) {
            chkPin = false;
        }
        MpsMarket mpsMarket = mpsMarketRepository.findMpsMarketByMid(mid).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));
        if ("N".equalsIgnoreCase(mpsMarket.getPinVrifyYn())) {
            chkPin = false;
        }
        /* 핀번호, 빌키 검증 */
        if (chkPin) {
            String pinNo = walletUseEachRequestDto.getPinNo();
            boolean isBillKeyUsed = "Y".equals(mpsMarket.getBillKeyUseYn());
            boolean hasBillKey = !CommonUtil.nullTrim(customerDto.getBillKeyEnc()).isEmpty();
            boolean isPinFormat = StringUtils.isNotBlank(pinNo) && pinNo.length() == 6;

            ChkPinErrorCountResponseDto chkPinErrorCountResponseDto = null;
            ChkPinErrorCountRequestDto chkPinErrorCountRequestDto = ChkPinErrorCountRequestDto.builder().pin(pinNo).trdNo(trdNo).customerDto(customerDto).mpsMarket(mpsMarket).build();

            if (isBillKeyUsed && hasBillKey && !isPinFormat) {
                chkPinErrorCountResponseDto = billKeyService.isCorrectBillkey(chkPinErrorCountRequestDto);
            } else {
                chkPinErrorCountResponseDto = authenticationService.isCorrectWhiteLabelPin(chkPinErrorCountRequestDto);
            }

            /* 거래실패 SET */
            if (!PinVerifyResult.SUCCESS.equals(chkPinErrorCountResponseDto.getPinVerifyResult())) {
                TradeFailInsertDto trdFailDto = TradeFailInsertDto.builder()
                        .trdNo(trdNo)//거래번호
                        .trdDivCd(TrdDivCd.COMMON_USE.getTrdDivCd())//거래구분코드
                        .mid(mid)
                        .amtSign(-1)
                        .trdAmt(mnyAmt + pntAmt)
                        .mnyBlc(mnyBlc)
                        .pntBlc(pntBlc)
                        .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                        .reqDtm(reqDtm)
                        .mTrdNo(mTrdNo)
                        .custNo(mpsCustNo)
                        .mCustId(mCustId)
                        .csrcIssReqYn(csrcIssReqYn)
                        .csrcIssStatCd("N")
                        .failDt(curDt)//거래일자
                        .failTm(curTm)//거래시간
                        .stlMId(stlMId)
                        .errCd(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorCode())
                        .errMsg(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorMessage() + chkPinErrorCountResponseDto.getPinVerifyResultMsgForTrdFail())
                        .storCd(storCd)
                        .storNm(storNm)
                        .prdtCd(MpsPrdtCd.use.getPrdtCd())
                        .build();
                tradeFailService.insertTradeFail(trdFailDto);

                throw new RequestValidationException(ErrorCode.PIN_NOT_MATCHED, chkPinErrorCountResponseDto.getPinVerifyResultMsg());
            }
        }

        //프로시저 호출을 위해 In변수 담는 객체 Build
        UseEachIn useEachInParam = UseEachIn.builder()
                .inMpsCustNo(mpsCustNo)
                .inTrdDivCd(TrdDivCd.COMMON_USE.getTrdDivCd())
                .inUseTrdNo(trdNo)
                .inUseTrdDt(curDt)
                .inMnyAmt(mnyAmt)
                .inPntAmt(pntAmt)
                .inBlc(inBlc)
                .inWorkerID(SERVER_ID)
                .inWorkerIP(SERVER_IP)
                .build();
        UseEachOut useEachOut = tradeRepository.useEach(useEachInParam);

        long resCode = useEachOut.getOutResCd();
        String resMsg = useEachOut.getOutResMsg();
        log.info("선불금 사용 EXEC => 회원번호:[{}] 결과코드:[{}] 결과메세지:[{}]", mpsCustNo, resCode, resMsg);

        if (resCode == ProcResCd.SUCCESS.getResCd()) {
            //거래 성공 쌓기

            long outMnyAmt = useEachOut.getOutMnyAmt();
            long outPntAmt = useEachOut.getOutPntAmt();
            long outMnyBlc = useEachOut.getOutMnyBlc();
            long outPntBlc = useEachOut.getOutPntBlc();
            long outWaitMnyBlc = useEachOut.getOutWaitMnyBlc();

            /* 포인트만 사용시에 현금영수증 발행 N */
            if (outMnyAmt == 0) csrcIssReqYn = "N";

            Trade trade = Trade.builder()
                    .trdNo(trdNo)//거래번호
                    .trdDt(curDt)//거래일자
                    .trdTm(curTm)//거래시간
                    .trdDivCd(TrdDivCd.COMMON_USE.getTrdDivCd())//거래구분코드
                    .svcCd(MpsApiCd.SVC_CD)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .mid(mid)
                    .amtSign(-1)//사용부호 사용이므로 -
                    .trdAmt(mnyAmt + pntAmt)//거래금액
                    .mnyAmt(outMnyAmt)//프로시저 결과값내 머니 사용금액
                    .pntAmt(outPntAmt)//프로시저 결과값내 포인트 사용금액
                    .waitMnyAmt(0)//대기머니 금액 X
                    .mnyBlc(outMnyBlc)//프로시저 결과값내 머니 잔액
                    .pntBlc(outPntBlc)//프로시저 결과값내 포인트 잔액
                    .waitMnyBlc(outWaitMnyBlc)//프로시저 결과값내 대기머니 잔액
                    .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())//충전수단 코드
                    .mReqDtm(reqDtm)//가맹점요청일시
                    .mTrdNo(mTrdNo)//가맹점 요청거래번호
                    .mpsCustNo(mpsCustNo)//회원번호
                    .mCustId(mCustId) //회원아이디
                    .csrcIssReqYn(csrcIssReqYn)//현금영수증 발행 Yn
                    .csrcIssStatCd("N")//현금영수증 발행 상태 N
                    .stlMId(stlMId)//정산 상점 아이디
                    .storCd(storCd)
                    .storNm(storNm)
                    .mResrvField1(walletUseEachRequestDto.getMResrvField1())
                    .mResrvField2(walletUseEachRequestDto.getMResrvField2())
                    .mResrvField3(walletUseEachRequestDto.getMResrvField3())
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

                    String formatTrdAmt = CommonUtil.formatMoney(outMnyAmt + outPntAmt);
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
                            .mid(mid)
                            .storNm(storNm)
                            .stlMid(stlMId)
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

            return WalletUseEachResponseDto.builder()
                    .custNo(mpsCustNo)
                    .mTrdNo(mTrdNo)
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdAmt(String.valueOf(outMnyAmt + outPntAmt))
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
                    .trdDivCd(TrdDivCd.COMMON_USE.getTrdDivCd())//거래구분코드
                    .mid(mid)
                    .amtSign(-1)
                    .trdAmt(mnyAmt + pntAmt)
                    .mnyBlc(mnyBlc)
                    .pntBlc(pntBlc)
                    .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                    .reqDtm(reqDtm)
                    .mTrdNo(mTrdNo)
                    .custNo(mpsCustNo)
                    .mCustId(mCustId)
                    .csrcIssReqYn(csrcIssReqYn)
                    .csrcIssStatCd("N")
                    .failDt(curDt)//거래일자
                    .failTm(curTm)//거래시간
                    .stlMId(stlMId)
                    .errCd(String.valueOf(resCode))
                    .errMsg(resMsg)
                    .storCd(storCd)
                    .storNm(storNm)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .build();
            tradeFailService.insertTradeFail(trdFailDto);
            throw new RequestValidationException(errorCode);
        }
    }

    /* 포인트 -> 머니 전환  */
    @Transactional(rollbackFor = Exception.class)
    public TransferWalletResponseDto transferWallet(TransferWalletRequestDto transferWalletRequestDto) throws NoSuchAlgorithmException {

        //잔액사용순서 default P
        String blcUseOrd = "P";
        if (StringUtils.isNotEmpty(transferWalletRequestDto.getBlcUseOrd())) {
            blcUseOrd = transferWalletRequestDto.getBlcUseOrd().toUpperCase();
        }

        //잔액 사용순서 검증
        if (!blcUseOrd.equals("P")) { //TODO 추후 머니->포인트 전환 오픈 시 수정
            throw new RequestValidationException(ErrorCode.TRANSFER_ERROR);
        }

        //회원 추출
        CustomerDto customerDto = transferWalletRequestDto.getCustomerDto();

        //변수 선언
        String trdNo = sequenceService.generateTradeSeq01();
        String mTrdNo = transferWalletRequestDto.getMTrdNo();

        CustomDateTimeUtil today = new CustomDateTimeUtil();
        String curDt = today.getDate();
        String curTm = today.getTime();

        String mpsCustNo = customerDto.getMpsCustNo();
        String mid = customerDto.getMid();
        String mCustId = customerDto.getMCustId();
        String trdDivCd = TrdDivCd.TRANSFER.getTrdDivCd(); //고정

        long trdAmt = Long.parseLong(transferWalletRequestDto.getTrdAmt());
        long mnyBlc = Long.parseLong(transferWalletRequestDto.getMnyBlc());
        long pntBlc = Long.parseLong(transferWalletRequestDto.getPntBlc());
        long mnyAmt = 0;
        long pntAmt = 0;

        //금액 음수 검증
        if (trdAmt < 0 || mnyBlc < 0 || pntBlc < 0) {
            throw new RequestValidationException(ErrorCode.AMT_CANNOT_BE_NEGATIVE);
        }
        String reqDtm = curDt + curTm;
        if (StringUtils.isNotEmpty(transferWalletRequestDto.getReqDt()) && StringUtils.isNotEmpty(transferWalletRequestDto.getReqTm())) {
            reqDtm = transferWalletRequestDto.getReqDt() + transferWalletRequestDto.getReqTm();
        }

        /* 핀번호, 빌키 검증 */
        MpsMarket mpsMarket = mpsMarketRepository.findMpsMarketByMid(mid).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));

        if ("N".equalsIgnoreCase(mpsMarket.getPinVrifyYn())) {
            //검증안하는 가맹점
            log.info("###### CheckPin [End] => mId:[{}] pinVrifyYn is N", mid);
        } else {
            String pinNo = transferWalletRequestDto.getPinNo();
            boolean isBillKeyUsed = "Y".equals(mpsMarket.getBillKeyUseYn());
            boolean hasBillKey = !CommonUtil.nullTrim(customerDto.getBillKeyEnc()).isEmpty();
            boolean isPinFormat = StringUtils.isNotBlank(pinNo) && pinNo.length() == 6;

            ChkPinErrorCountResponseDto chkPinErrorCountResponseDto = null;
            ChkPinErrorCountRequestDto chkPinErrorCountRequestDto = ChkPinErrorCountRequestDto.builder().pin(pinNo).trdNo(trdNo).customerDto(customerDto).mpsMarket(mpsMarket).build();

            if (isBillKeyUsed && hasBillKey && !isPinFormat) {
                chkPinErrorCountResponseDto = billKeyService.isCorrectBillkey(chkPinErrorCountRequestDto);
            } else {
                chkPinErrorCountResponseDto = authenticationService.isCorrectWhiteLabelPin(chkPinErrorCountRequestDto);
            }

            /* 거래실패 SET */
            if (!PinVerifyResult.SUCCESS.equals(chkPinErrorCountResponseDto.getPinVerifyResult())) {
                TradeFailInsertDto trdFailDto = TradeFailInsertDto.builder()
                        .trdNo(trdNo)//거래번호
                        .trdDivCd(trdDivCd)//거래구분코드
                        .mid(mid)
                        .amtSign(-1)
                        .trdAmt(trdAmt)
                        .mnyBlc(mnyBlc)
                        .pntBlc(pntBlc)
                        .chrgMeanCd(TrdChrgMeanCd.POINT_TO_MONEY.getChrgMeanCd())
                        .reqDtm(reqDtm)
                        .mTrdNo(mTrdNo)
                        .custNo(mpsCustNo)
                        .mCustId(mCustId)
                        .csrcIssReqYn("N")
                        .csrcIssStatCd("N")
                        .failDt(curDt)//거래일자
                        .failTm(curTm)//거래시간
                        .stlMId(mid)
                        .errCd(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorCode())
                        .errMsg(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorMessage() + chkPinErrorCountResponseDto.getPinVerifyResultMsgForTrdFail())
                        .storCd("N")
                        .storNm("N")
                        .prdtCd(MpsPrdtCd.use.getPrdtCd())
                        .build();
                tradeFailService.insertTradeFail(trdFailDto);
                throw new RequestValidationException(ErrorCode.PIN_NOT_MATCHED, chkPinErrorCountResponseDto.getPinVerifyResultMsg());
            }
        }

        /* 기명검증 */
        if (!customerDto.getCustDivCd().equals(CustDivCd.NAMED.getCustDivCd())) {
            throw new RequestValidationException(ErrorCode.CUSTOMER_UNINSCRIBED);
        }

        /* 금액 검증 */
        CustomerWalletResponseDto custWallet = getCustWalletByCustNo(mpsCustNo);
        if (blcUseOrd.equals("P")) {
            pntAmt = trdAmt;
            if (custWallet.getPntBlc() < trdAmt) {
                throw new RequestValidationException(ErrorCode.TRANSFER_AMT_ERROR);
            }
        } else {
            mnyAmt = trdAmt;
            if (custWallet.getMnyBlc() < trdAmt) {
                throw new RequestValidationException(ErrorCode.TRANSFER_AMT_ERROR);
            }
        }

        /* 한도 검증 */
        if (blcUseOrd.equals("P")) {
            /* 머니 한도체크 */
            if (custWallet.getMnyBlc() + trdAmt > customerDto.getChrgLmtAmlt()) {
                throw new RequestValidationException(ErrorCode.TRANSFER_LIMIT_REACHED);
            }
        }

        //프로시저 호출을 위해 In변수 담는 객체 Build
        UseEachIn useEachInParam = UseEachIn.builder()
                .inMpsCustNo(mpsCustNo)
                .inTrdDivCd(TrdDivCd.COMMON_USE.getTrdDivCd())
                .inUseTrdNo(trdNo)
                .inUseTrdDt(curDt)
                .inMnyAmt(mnyAmt)
                .inPntAmt(pntAmt)
                .inBlc(mnyBlc + pntBlc)
                .inWorkerID(SERVER_ID)
                .inWorkerIP(SERVER_IP)
                .build();
        UseEachOut useEachOut = tradeRepository.useEach(useEachInParam);

        long resCode = useEachOut.getOutResCd();
        String resMsg = useEachOut.getOutResMsg();
        log.info("[포인트 -> 머니 전환] 선불금 사용 EXEC => 회원번호:[{}] 결과코드:[{}] 결과메세지:[{}]", mpsCustNo, resCode, resMsg);

        ErrorCode errorCode;

        if (resCode != ProcResCd.SUCCESS.getResCd()) {
            TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
            tradeFailInsertDto.setTrdNo(trdNo);
            tradeFailInsertDto.setCustNo(mpsCustNo);
            tradeFailInsertDto.setFailDt(curDt);
            tradeFailInsertDto.setFailTm(curTm);
            tradeFailInsertDto.setMCustId(mCustId);
            tradeFailInsertDto.setTrdDivCd(TrdDivCd.TRANSFER.getTrdDivCd());
            tradeFailInsertDto.setMid(customerDto.getMid());
            tradeFailInsertDto.setMTrdNo(transferWalletRequestDto.getMTrdNo());
            tradeFailInsertDto.setAmtSign(-1);
            tradeFailInsertDto.setTrdAmt(trdAmt);
            tradeFailInsertDto.setMnyAmt(mnyAmt);
            tradeFailInsertDto.setPntAmt(pntAmt);
            tradeFailInsertDto.setCustBdnFeeAmt(0);
            tradeFailInsertDto.setWaitMnyAmt(0);
            tradeFailInsertDto.setMnyBlc(Long.parseLong(transferWalletRequestDto.getMnyBlc()));
            tradeFailInsertDto.setPntBlc(Long.parseLong(transferWalletRequestDto.getPntBlc()));
            tradeFailInsertDto.setWaitMnyBlc(0);
            tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.POINT_TO_MONEY.getChrgMeanCd());
            tradeFailInsertDto.setReqDtm(reqDtm);
            tradeFailInsertDto.setErrCd(String.valueOf(resCode));
            tradeFailInsertDto.setErrMsg(resMsg);
            tradeFailInsertDto.setPrdtCd(MpsPrdtCd.use.getPrdtCd());
            log.info("전환 거래실패 data : {} ", tradeFailInsertDto);
            tradeFailService.insertTradeFail(tradeFailInsertDto);

            errorCode = ErrorCode.SERVER_ERROR_CODE;

            if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
                errorCode = ErrorCode.BALANCE_NOT_MATCHED;
            } else if (resCode == ProcResCd.REQ_AMT_NOT_MATCHED.getResCd()) {
                errorCode = ErrorCode.REQ_AMT_NOT_MATCHED;
            } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd()) {
                errorCode = ErrorCode.TRADE_AMT_ERROR;
            } else if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
                throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
            }
            throw new RequestValidationException(errorCode, " [포인트 전환 오류]");
        }

        ChargeApprovalResponseDto chargeApprovalResponseDto = null;

        if (resCode == ProcResCd.SUCCESS.getResCd()) {

            /* 포인트 사용 거래 */
            tradeRepository.save(Trade.builder()
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdDivCd(TrdDivCd.TRANSFER.getTrdDivCd())
                    .svcCd(MpsApiCd.SVC_CD)
                    .mCustId(mCustId)
                    .prdtCd(MpsPrdtCd.use.getPrdtCd())
                    .mid(mid)
                    .amtSign(-1)
                    .trdAmt(trdAmt)
                    .mnyAmt(mnyAmt)
                    .pntAmt(pntAmt)
                    .waitMnyAmt(0)
                    .mnyBlc(useEachOut.getOutMnyBlc())
                    .pntBlc(useEachOut.getOutPntBlc())
                    .waitMnyBlc(useEachOut.getOutWaitMnyBlc())
                    .custBdnFeeAmt(0)
                    .blcUseOrd("P")
                    .csrcIssReqYn("N")
                    .cnclYn("N")
                    .storCd("N")
                    .storNm("N")
                    .stlMId(mid)
                    .chrgMeanCd(TrdChrgMeanCd.POINT_TO_MONEY.getChrgMeanCd())
                    .mReqDtm(reqDtm)
                    .mTrdNo(transferWalletRequestDto.getMTrdNo())
                    .mpsCustNo(mpsCustNo)
                    .mResrvField1(transferWalletRequestDto.getMResrvField1())
                    .mResrvField2(transferWalletRequestDto.getMResrvField2())
                    .mResrvField3(transferWalletRequestDto.getMResrvField3())
                    .createdIp(ServerInfoConfig.HOST_IP)
                    .createdId(ServerInfoConfig.HOST_NAME)
                    .build());

            /* 머니 충전 */
            MarketAddInfoDto marketAddInfo = commonService.getMarketAddInfoByMId(customerDto.getMid());
            String pktHashStr = customerDto.getMpsCustNo() + customerDto.getMid() + transferWalletRequestDto.getMTrdNo() + trdAmt;
            String pktHash = digestSHA256(pktHashStr + marketAddInfo.getPktHashKey());

            try {
                ChargeApprovalRequestDto chargeApprovalRequestDto = ChargeApprovalRequestDto.builder()
                        .custNo(mpsCustNo)
                        .customerDto(customerDto)
                        .mTrdNo(transferWalletRequestDto.getMTrdNo())
                        .chrgMeanCd(TrdChrgMeanCd.POINT_TO_MONEY.getChrgMeanCd())
                        .trdAmt(String.valueOf(trdAmt))
                        .divCd(TrdDivCd.MONEY_PROVIDE.getTrdDivCd())
                        .blcAmt(String.valueOf(useEachOut.getOutMnyBlc()))
                        .chrgTrdNo(trdNo)
                        .mResrvField1(transferWalletRequestDto.getMResrvField1())
                        .mResrvField2(transferWalletRequestDto.getMResrvField2())
                        .mResrvField3(transferWalletRequestDto.getMResrvField3())
                        .pktHash(pktHash)
                        .build();
                chargeApprovalResponseDto = approvalService.chargeApproval(chargeApprovalRequestDto);
            } catch (Exception e) {
                errorCode = ErrorCode.fromErrorMessage(e.getMessage());

                TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
                tradeFailInsertDto.setTrdNo(trdNo);
                tradeFailInsertDto.setCustNo(mpsCustNo);
                tradeFailInsertDto.setFailDt(curDt);
                tradeFailInsertDto.setFailTm(curTm);
                tradeFailInsertDto.setMCustId(mCustId);
                tradeFailInsertDto.setTrdDivCd(TrdDivCd.TRANSFER.getTrdDivCd());
                tradeFailInsertDto.setMid(customerDto.getMid());
                tradeFailInsertDto.setMTrdNo(transferWalletRequestDto.getMTrdNo());
                tradeFailInsertDto.setAmtSign(-1);
                tradeFailInsertDto.setTrdAmt(trdAmt);
                tradeFailInsertDto.setMnyAmt(mnyAmt);
                tradeFailInsertDto.setPntAmt(pntAmt);
                tradeFailInsertDto.setCustBdnFeeAmt(0);
                tradeFailInsertDto.setWaitMnyAmt(0);
                tradeFailInsertDto.setMnyBlc(Long.parseLong(transferWalletRequestDto.getMnyBlc()));
                tradeFailInsertDto.setPntBlc(Long.parseLong(transferWalletRequestDto.getPntBlc()));
                tradeFailInsertDto.setWaitMnyBlc(0);
                tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.POINT_TO_MONEY.getChrgMeanCd());
                tradeFailInsertDto.setReqDtm(reqDtm);
                tradeFailInsertDto.setErrCd(errorCode.getErrorCode());
                tradeFailInsertDto.setErrMsg(e.getMessage());
                tradeFailInsertDto.setPrdtCd(MpsPrdtCd.use.getPrdtCd());
                log.info("전환-충전 거래 중 거래실패 data : {} ", tradeFailInsertDto);
                tradeFailService.insertTradeFail(tradeFailInsertDto);
                e.printStackTrace();
                throw new RequestValidationException(ErrorCode.fromErrorMessage(e.getMessage()), " [포인트 전환 오류]");
            }

            if (chargeApprovalResponseDto != null) {

                /* 사용 성공 충전 알림 메일 전송 */
                if (!CommonUtil.nullTrim(customerDto.getEmail()).equals("")) {
                    try {
                        String trdDtm;
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        trdDtm = now.format(formatter);

                        String formatTrdAmt = CommonUtil.formatMoney(trdAmt);
                        String formatOutMnyAmt = CommonUtil.formatMoney(0);
                        String formatOutPntAmt = CommonUtil.formatMoney(trdAmt);

                        PyNtcSendInsertRequestDto pyNtcSendInsertRequestDto = PyNtcSendInsertRequestDto.builder()
                                .mpsCustNo(mpsCustNo)
                                .custNm(customerDto.getCustNm())
                                .email(customerDto.getEmail())
                                .trdNo(trdNo)
                                .amt(formatTrdAmt)
                                .mnyAmt(formatOutMnyAmt)
                                .pntAmt(formatOutPntAmt)
                                .trdDtm(trdDtm)
                                .mid(mid)
                                .storNm("N")
                                .stlMid(mid)
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
            } else {
                throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE, " [포인트 전환 오류]");
            }
        }

        return TransferWalletResponseDto.builder()
                .custNo(mpsCustNo)
                .mTrdNo(transferWalletRequestDto.getMTrdNo())
                .trdNo(trdNo)
                .trdDt(curDt)
                .trdTm(curTm)
                .trdAmt(String.valueOf(trdAmt))
                .mnyBlc(chargeApprovalResponseDto.getMnyBlc())
                .pntBlc(String.valueOf(useEachOut.getOutPntBlc()))
                .build();
    }

    /* 충전건별 잔액조회 */
    @Transactional(readOnly = true)
    public WalletBalanceByLedgerResponseDto getCustWalletByLedger(WalletBalanceByLedgerRequestDto walletBalanceByLedgerRequestDto) {

        if (!"M".equals(walletBalanceByLedgerRequestDto.getBlcDivCd()) && !"P".equals(walletBalanceByLedgerRequestDto.getBlcDivCd())) {
            throw new RequestValidationException(ErrorCode.BLC_DIV_CD_ERROR, "'P' 또는 'M' 만 가능합니다.");
        }

        long blcAmt = 0;
        long chrgAmt = 0;
        String trdNo = walletBalanceByLedgerRequestDto.getTrdNo();
        String trdDt = walletBalanceByLedgerRequestDto.getTrdDt();
        String blcDivCd = walletBalanceByLedgerRequestDto.getBlcDivCd();

        Trade tgtTrade = tradeRepository.findByTrdNoAndTrdDt(trdNo, trdDt);
        if (tgtTrade == null) {
            throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_NOT_FOUND);
        }

        try {
            if ("P".equals(blcDivCd)) {
                blcAmt = payPntRepository.getPntBlcByLedger(trdDt, trdNo);
                chrgAmt = tgtTrade.getPntAmt();
            } else if ("M".equals(blcDivCd)) {
                blcAmt = payMnyRepository.getMnyBlcByLedger(trdDt, trdNo);
                chrgAmt = tgtTrade.getMnyAmt();
            }
        } catch (Exception e) {
            throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_NOT_FOUND);
        }

        WalletBalanceByLedgerResponseDto walletBalanceByLedgerResponseDto = WalletBalanceByLedgerResponseDto.builder()
                .custNo(walletBalanceByLedgerRequestDto.getCustNo())
                .chrgAmt(String.valueOf(chrgAmt))
                .blcAmt(String.valueOf(blcAmt))
                .trdNo(trdNo)
                .build();
        return walletBalanceByLedgerResponseDto;
    }

    @Transactional(readOnly = true)
    public WalletOptionBalanceResponseDto getWalletOptionBalance(WalletOptionBalanceRequestDto walletOptionBalanceRequestDto) {
        CustomerDto customerDto = walletOptionBalanceRequestDto.getCustomerDto();
        String custNo = walletOptionBalanceRequestDto.getCustNo();


        if (!autoChargeAvailabilityValidService.isAutoChargeableMarket(customerDto.getMid())) {
            throw new RequestValidationException(ErrorCode.NOT_AUTO_CHRG_MARKET);
        }
        CustomerWalletResponseDto customerWalletResponseDto = getCustWalletByCustNoNew(custNo, customerDto);

        String custAutoChrgYn = "N";
        CustChrgMeanDto custChrgMeanDto = custChrgMeanService.getCustChrgMeanForAutoChrg(custNo);

        //자동충전 Y, 부족분 Y 인경우에만 결과값 Y
        if (custChrgMeanDto.isAutoChargeUse() && custChrgMeanDto.isShortageEnabled()) {
            custAutoChrgYn = "Y";
        }

        return WalletOptionBalanceResponseDto.builder()
                .custNo(custNo)
                .custDivCd(customerWalletResponseDto.getCustDivCd())
                .chrgblAmt(String.valueOf(customerWalletResponseDto.getChrgblAmt()))
                .mnyBlc(String.valueOf(customerWalletResponseDto.getMnyBlc()))
                .pntBlc(String.valueOf(customerWalletResponseDto.getPntBlc()))
                .waitMnyBlc(String.valueOf(customerWalletResponseDto.getWaitMnyBlc()))
                .expMnyBlc(String.valueOf(customerWalletResponseDto.getExpMnyBlc()))
                .custAutoChrgYn(custAutoChrgYn)
                .build();
    }
}
