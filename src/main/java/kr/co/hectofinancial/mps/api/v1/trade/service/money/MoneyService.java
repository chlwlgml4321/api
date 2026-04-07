package kr.co.hectofinancial.mps.api.v1.trade.service.money;

import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountRequestDto;
import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountResponseDto;
import kr.co.hectofinancial.mps.api.v1.authentication.service.AuthenticationService;
import kr.co.hectofinancial.mps.api.v1.authentication.service.BillKeyService;
import kr.co.hectofinancial.mps.api.v1.common.dto.GetCustChrgMeanResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.firm.dto.RemittanceApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.firm.dto.RemittanceResultDto;
import kr.co.hectofinancial.mps.api.v1.firm.repository.DpmnNotiRepository;
import kr.co.hectofinancial.mps.api.v1.firm.service.FirmBankingService;
import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarket;
import kr.co.hectofinancial.mps.api.v1.market.repository.MpsMarketRepository;
import kr.co.hectofinancial.mps.api.v1.trade.domain.RfdRcpt;
import kr.co.hectofinancial.mps.api.v1.trade.domain.RfdRsltCnf;
import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeFailInsertDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminWithdrawApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminWithdrawApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.*;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.CustomerWalletResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Pay.PayIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Pay.PayOut;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseEach.UseEachIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseEach.UseEachOut;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Withdrawal.WithdrawalIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Withdrawal.WithdrawalOut;
import kr.co.hectofinancial.mps.api.v1.trade.repository.RfdRcptRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.RfdRsltCnfRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class MoneyService {

    @Value("${spring.profiles.active}")
    private String profiles;
    @Value("${rmt.mid}")
    private String rfdMid;

    private final WalletService walletService;
    private final TradeRepository tradeRepository;
    private final TradeFailService tradeFailService;
    private final CommonService commonService;
    private final MpsMarketRepository mpsMarketRepository;
    private final SequenceService sequenceService;
    private final RfdRcptInsertService rfdRcptInsertService;
    private final RfdRcptRepository rfdRcptRepository;
    private final FirmBankingService firmBankingService;
    private final RfdRsltCnfInsertService rfdRsltCnfInsertService;
    private final RfdRsltCnfRepository rfdRsltCnfRepository;
    private final AuthenticationService authenticationService;
    private final BillKeyService billKeyService;
    private final DpmnNotiRepository dpmnNotiRepository;

    /* 머니 출금 */
    @Transactional(rollbackFor = Exception.class)
    public WithdrawApprovalResponseDto moneyWithdraw(WithdrawApprovalRequestDto withdrawApprovalRequestDto) throws Exception {

        log.info("회원번호: [{}], 상점거래번호: [{}], 요청금액: [{}]", withdrawApprovalRequestDto.getCustomerDto().getMpsCustNo(), withdrawApprovalRequestDto.getMTrdNo(), withdrawApprovalRequestDto.getTrdAmt());

        //거래구분코드 검증 최상단
        if (!withdrawApprovalRequestDto.getDivCd().equals(TrdDivCd.MONEY_WITHDRAW.getTrdDivCd())) {
            if ("_X_CANCEL_WITHDRAWAL_".equals(withdrawApprovalRequestDto.getDivCd())) {//선불홈->서비스해지를 위한 해지 출금
                withdrawApprovalRequestDto.setDivCd(TrdDivCd.TERMINATE_WITHDRAW.getTrdDivCd());
            }else{
                throw new RequestValidationException(ErrorCode.TRADE_DIV_CD_ERROR);
            }
        }
        /* 일자 시각 SET */
        String reqDtm = "";
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();
        if (CommonUtil.nullTrim(withdrawApprovalRequestDto.getReqDt()).equals("")) {
            reqDtm = curDt + curTm;
        } else {
            String reqTm = (withdrawApprovalRequestDto.getReqTm() == null ? curTm : withdrawApprovalRequestDto.getReqTm());
            reqDtm = withdrawApprovalRequestDto.getReqDt() + reqTm;
        }

        //회원 추출
        CustomerDto customerDto = withdrawApprovalRequestDto.getCustomerDto();
        String mCustId = withdrawApprovalRequestDto.getCustomerDto().getMCustId();
        String trdNo = "";
        trdNo = sequenceService.generateTradeSeq01();
        String custNo = withdrawApprovalRequestDto.getCustNo();
        String mid = withdrawApprovalRequestDto.getCustomerDto().getMid();
        String trdDivCd = withdrawApprovalRequestDto.getDivCd();
        String blcUseOrd = TrBlcUseOrd.MONEY.getBlcUseOrd();
        Long waitAmt = 0L;
        long custBdnFee = 0L;
        long totWdAmt = 0L;
        String rfdRcptNo = null;
        String chrgTrdNo = null;

        if (CommonUtil.compareStrings(curTm, "002000") <= 0 || CommonUtil.compareStrings(curTm, "235000") >= 0) {
            throw new RequestValidationException(ErrorCode.BANK_INSPECTION);
        }

        /* 핀번호, 빌키 검증 */
        MpsMarket mpsMarket = mpsMarketRepository.findMpsMarketByMid(mid).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));

        if ("N".equalsIgnoreCase(mpsMarket.getPinVrifyYn())) {
            //검증안하는 가맹점
            log.info("###### CheckPin [End] => mId:[{}] pinVrifyYn is N", mid);
        } else {
            String pinNo = withdrawApprovalRequestDto.getPinNo();
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
                TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
                tradeFailInsertDto.setTrdNo(trdNo);
                tradeFailInsertDto.setCustNo(custNo);
                tradeFailInsertDto.setFailDt(curDt);
                tradeFailInsertDto.setFailTm(curTm);
                tradeFailInsertDto.setTrdDivCd(trdDivCd);
                tradeFailInsertDto.setMid(mid);
                tradeFailInsertDto.setMCustId(mCustId);
                tradeFailInsertDto.setMTrdNo(withdrawApprovalRequestDto.getMTrdNo());
                tradeFailInsertDto.setAmtSign(-1);
                tradeFailInsertDto.setTrdAmt(Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()));
                tradeFailInsertDto.setMnyAmt(Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()));
                tradeFailInsertDto.setPntAmt(0);
                tradeFailInsertDto.setWaitMnyAmt(waitAmt);
                tradeFailInsertDto.setMnyBlc(Long.parseLong(withdrawApprovalRequestDto.getMnyBlc()));
                tradeFailInsertDto.setPntBlc(0);
                tradeFailInsertDto.setWaitMnyBlc(0);
                tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
                tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
                tradeFailInsertDto.setReqDtm(reqDtm);
                tradeFailInsertDto.setTrdSumry("머니출금");
                tradeFailInsertDto.setErrCd(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorCode());
                tradeFailInsertDto.setErrMsg(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorMessage() + chkPinErrorCountResponseDto.getPinVerifyResultMsgForTrdFail());
                tradeFailInsertDto.setPrdtCd(MpsPrdtCd.withdrawal.getPrdtCd());
                log.info("머니출금 거래실패 data : {} ", tradeFailInsertDto);
                tradeFailService.insertTradeFail(tradeFailInsertDto);

                throw new RequestValidationException(ErrorCode.PIN_NOT_MATCHED, chkPinErrorCountResponseDto.getPinVerifyResultMsg());
            }
        }
        /* TB_MPS_M joinType 검증 */
        if(!mpsMarket.getCustJoinTypeCd().equals("1")){
            if (withdrawApprovalRequestDto.getCustomerDto().getCustDivCd().equals(CustDivCd.ANONYMOUS.getCustDivCd())) {
                throw new RequestValidationException(ErrorCode.CUSTOMER_UNINSCRIBED);
            }
        }

        /* 수수료 SET */
        if (withdrawApprovalRequestDto.getCustBdnFeeAmt() != null) {
            custBdnFee = Long.parseLong(withdrawApprovalRequestDto.getCustBdnFeeAmt());
        }
        //금액검증
        if (CommonUtil.nullTrim(withdrawApprovalRequestDto.getTrdAmt()).equals("0")) {
            throw new RequestValidationException(ErrorCode.TRADE_AMT_ERROR);
        }
        if (custBdnFee < 0) {
            throw new RequestValidationException(ErrorCode.CUST_FEE_AMT_ERROR);
        }

        if (CommonUtil.nullTrim(withdrawApprovalRequestDto.getDivCd()).equals(TrdDivCd.MONEY_WITHDRAW.getTrdDivCd())) {

            /* 출금가능금액조회, 출금 횟수 검증 */
            WithdrawalMoneyRequestDto withdrawalMoneyRequestDto = new WithdrawalMoneyRequestDto();
            withdrawalMoneyRequestDto.setCustNo(custNo);
            withdrawalMoneyRequestDto.setCustomerDto(withdrawApprovalRequestDto.getCustomerDto());
            WithdrawalMoneyResponseDto withdrawalMoneyResponseDto = walletService.getWithdrawalMoneyAmt(withdrawalMoneyRequestDto);

            if (Long.parseLong(withdrawalMoneyResponseDto.getWdMnyAmt()) < Long.parseLong(withdrawApprovalRequestDto.getTrdAmt())) {
                throw new RequestValidationException(ErrorCode.WITHDRAW_AMT_ERROR);
            }
            log.info("출금 횟수: [{}]", withdrawalMoneyResponseDto.getMwCount());
            log.info("선불상점 출금 최대 횟수: [{}]", mpsMarket.getMonWdLmtCnt());
            if(mpsMarket.getMonWdLmtCnt() != -1){ // -1은 무제한
                if (Long.parseLong(withdrawalMoneyResponseDto.getMwCount()) >= mpsMarket.getMonWdLmtCnt()) {
                    throw new RequestValidationException(ErrorCode.WITHDRAW_MONEYCANCEL_FAIL);
                }
            }
        }

        /* 회원 잔액조회 */
        CustomerWalletResponseDto custWallet = walletService.getCustWalletByCustNo(custNo);
        Long pntAmt = custWallet.getPntBlc();

        //해지출금 수수료제외
        if (CommonUtil.nullTrim(withdrawApprovalRequestDto.getDivCd()).equals(TrdDivCd.TERMINATE_WITHDRAW.getTrdDivCd())) {
            custBdnFee = 0L;
            if(custWallet.getWaitMnyBlc() > 0) throw new RequestValidationException(ErrorCode.WAIT_MONEY_REMAINED);
            if (!withdrawApprovalRequestDto.getTrdAmt().equals(String.valueOf(custWallet.getMnyBlc()))) {
                if (Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()) > custWallet.getMnyBlc()) {
                    throw new RequestValidationException(ErrorCode.WITHDRAW_AMT_ERROR);
                } else {
                    throw new RequestValidationException(ErrorCode.TERMINATE_WITHDRAW_FAIL);
                }
            }
        }

        totWdAmt = (Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()) - custBdnFee);
        log.info("회원 부담 수수료: [{}]", custBdnFee);
        log.info("요청금액: [{}], 수수료포함 출금금액: [{}]", withdrawApprovalRequestDto.getTrdAmt(), totWdAmt);
        if (totWdAmt <= 0) {
            throw new RequestValidationException(ErrorCode.TRADE_AMT_ERROR);
        }

        /* 회원 계좌번호 조회 */
        GetCustChrgMeanResponseDto tgtCustMean = commonService.getCustChrgMean(custNo, TrdChrgMeanCd.RESISTERD_ACCOUNT.getChrgMeanCd());
        String custAccNo = tgtCustMean.getAccountNo();
        int strIndex = custAccNo.length();
        String custAcNoStr = custAccNo.substring(strIndex - 3);
        String bankCd = tgtCustMean.getBankCd();
        String custNm = tgtCustMean.getCustNm();
        String mskAccNo = CommonUtil.maskingString(custAccNo, 3, 3);

        CustomerDto tgtCustomer = withdrawApprovalRequestDto.getCustomerDto();

        /* 법인사업자일때 검증 로직 제외 */
        if("C".equals(tgtCustomer.getBizDivCd())){
            /* 사업자번호 검증 */
            if (!tgtCustMean.getBizRegNo().equals(tgtCustomer.getBizRegNo())) {
                throw new RequestValidationException(ErrorCode.WITHDRAW_BIZREGNO_ERROR);
            }
        }else{
            /* 예금주명 검증 */
            if (!CommonUtil.removeAllSpaces(tgtCustMean.getCustNm()).equals(CommonUtil.removeAllSpaces(tgtCustomer.getCustNm()))) {
                log.error(">>>출금 예금주명 검증<<< [{}], [{}]", tgtCustomer.getCustNm(), tgtCustMean.getCustNm());
                throw new RequestValidationException(ErrorCode.WITHDRAW_NAME_ERROR);
            }

            /* 생년월일 검증 */
            if (!tgtCustMean.getBirthDt().equals(tgtCustomer.getBirthDt())) {
                throw new RequestValidationException(ErrorCode.WITHDRAW_BIRTHDT_ERROR);
            }
        }

        /* 고객통장 적요 */
        String custTrdSumry = tgtCustMean.getCustNm();
        if(!CommonUtil.nullTrim(mpsMarket.getWdTrdSumry()).equals("")){
            custTrdSumry = mpsMarket.getWdTrdSumry();
        }
        custTrdSumry = CommonUtil.limitStringLength(custTrdSumry, 7); //전각문자기준 7자리까지

        /* 인출(사용) 프로시저 호출 */
        WithdrawalIn withdrawalIn = WithdrawalIn.builder()
                .inMpsCustNo(custNo)
                .inTrdDivCd(withdrawApprovalRequestDto.getDivCd())
                .inBlcUseOrd(blcUseOrd)
                .inUseTrdNo(trdNo)
                .inUseTrdDt(curDt)
                .inTrdAmt(Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()))
                .inBlc(Long.parseLong(withdrawApprovalRequestDto.getMnyBlc()) + pntAmt)
                .inWorkerID(ServerInfoConfig.HOST_NAME)
                .inWorkerIP(ServerInfoConfig.HOST_IP)
                .build();
        WithdrawalOut withdrawalOut = tradeRepository.withdrawal(withdrawalIn);

        long resCode = (long) withdrawalOut.getOutResCd();
        String resMsg = withdrawalOut.getOutResMsg();
        log.info("응답코드: [{}] 응답메시지: [{}]", resCode, resMsg);

        if (resCode == ProcResCd.SUCCESS.getResCd()) {

            boolean saveTrade = true;
            String trdSumry = null;

            if (mpsMarket.getWdTypeCd().equals(WdTypeCd.REAL_TIME.getWdTypeCd())) {
                /* 실시간환불(송금) */
                RemittanceApprovalRequestDto remittanceApprovalRequestDto = RemittanceApprovalRequestDto.builder()
                        .mchtId(rfdMid)
                        .mchtTrdNo(trdNo)//선불 주문번호
                        .mchtCustId(withdrawApprovalRequestDto.custNo)
                        .trdDt(curDt)
                        .trdTm(curTm)
                        .bankCd(tgtCustMean.getBankCd())
                        .custAcntNo(custAccNo)
                        .custAcntSumry(custTrdSumry)
                        .trdAmt(totWdAmt)
                        .macntSumry(custNo)
                        .build();

                RemittanceResultDto resultDto = firmBankingService.remittanceApproval(remittanceApprovalRequestDto);

                String outStatCd = resultDto.getOutStatCd();
                String outRsltCd = resultDto.getOutRsltCd();
                String outRsltMsg = resultDto.getOutRsltMsg();
                chrgTrdNo = resultDto.getTrdNo();
                trdSumry = outRsltCd;
                if (!outStatCd.equals("0021")) {
                    saveTrade = false;
                    boolean saveFailTrade = false;
                    String message = String.format("**ERROR 발생** 선불충전금 출금 / [ M_ID: " + mid + ", 선불회원번호: " + custNo + ", 금액: " + totWdAmt + ", outRsltCd: " + outRsltCd + ", outRsltMsg: " + outRsltMsg + "]");
                    MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);

                    /* VTIM: 타임아웃, ST38: 요청 진행 중, ST04: VAN 요청중 시스템 에러, ST06: 거래번호 정보가 없음 */
                    if (outRsltCd.equals("VTIM") || outRsltCd.equals("ST38") || outRsltCd.equals("ST04") || outRsltCd.equals("ST06")){
                        saveTrade = true;
                        RfdRsltCnfInsertDto rfdRsltCnfInsertDto = RfdRsltCnfInsertDto.builder()
                                .trdNo(trdNo)
                                .trdDt(curDt)
                                .trdTm(curTm)
                                .rfdTrdNo(chrgTrdNo)
                                .mid(mid)
                                .mpsCustNo(custNo)
                                .rsltCnfStatCd(RfdRsltCnfStatCd.RETRY.getRsltCnfStatCd())
                                .trdAmt(totWdAmt)
                                .rsltCnfCnt(0)
                                .rmk(outRsltCd)
                                .rfdAcntBankCd(bankCd)
                                .rfdAcntNoEnc(tgtCustMean.getAesAccountNo())
                                .rfdAcntNoMsk(mskAccNo)
                                .rmtDivCd(RmtDivCd.WITHDRAW.getRmtDivCd())
                                .build();
                        rfdRsltCnfInsertService.insertRfdRsltCnf(rfdRsltCnfInsertDto);
                        chrgTrdNo = null;
                    } else {
                        saveFailTrade = true;
                    }

                    if(saveFailTrade){
                        TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
                        tradeFailInsertDto.setTrdNo(trdNo);
                        tradeFailInsertDto.setCustNo(custNo);
                        tradeFailInsertDto.setFailDt(curDt);
                        tradeFailInsertDto.setFailTm(curTm);
                        tradeFailInsertDto.setTrdDivCd(trdDivCd);
                        tradeFailInsertDto.setMid(mid);
                        tradeFailInsertDto.setMCustId(mCustId);
                        tradeFailInsertDto.setMTrdNo(withdrawApprovalRequestDto.getMTrdNo());
                        tradeFailInsertDto.setAmtSign(-1);
                        tradeFailInsertDto.setTrdAmt(Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()));
                        tradeFailInsertDto.setMnyAmt(Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()));
                        tradeFailInsertDto.setPntAmt(0);
                        tradeFailInsertDto.setCustBdnFeeAmt(custBdnFee);
                        tradeFailInsertDto.setWaitMnyAmt(waitAmt);
                        tradeFailInsertDto.setMnyBlc(Long.parseLong(withdrawApprovalRequestDto.getMnyBlc()));
                        tradeFailInsertDto.setPntBlc(withdrawalOut.getOutPntBlc());
                        tradeFailInsertDto.setWaitMnyBlc(withdrawalOut.getOutWaitMnyBlc());
                        tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
                        tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
                        tradeFailInsertDto.setReqDtm(reqDtm);
                        tradeFailInsertDto.setTrdSumry("bankCd:" + tgtCustMean.getBankCd() + ";accNo:" + custAcNoStr);
                        tradeFailInsertDto.setErrCd(outStatCd);
                        tradeFailInsertDto.setErrMsg("outRsltCd:" + outRsltCd + ", trdNo:" + chrgTrdNo + ", outRsltMsg:" + outRsltMsg);
                        tradeFailInsertDto.setPrdtCd(MpsPrdtCd.withdrawal.getPrdtCd());
                        tradeFailInsertDto.setChrgTrdNo(chrgTrdNo);
                        log.info("머니출금 거래실패 data : {} ", tradeFailInsertDto);
                        tradeFailService.insertTradeFail(tradeFailInsertDto);
                        throw new RequestValidationException(ErrorCode.REMITTANCE_ERROR, outRsltMsg);
                    }
                }
            } else if(mpsMarket.getWdTypeCd().equals(WdTypeCd.BATCH.getWdTypeCd())){
                /* trd.TB_RFD_RCPT 테이블 INSERT */
                rfdRcptNo = sequenceService.generateRfdRcptSeq01();
                trdSumry = rfdRcptNo;
                chrgTrdNo = rfdRcptNo;

                /* 모계좌 정보 등록 */ //머니출금할때 거래적요에 찾아서 담기

                Object[] tgtMacntNo = (Object[]) dpmnNotiRepository.findByMAcntEnc(rfdMid);
                if (tgtMacntNo == null) {
                    String msg = "MPS_CUST_NO: " + custNo + ", 금액: " + totWdAmt + "M_TRD_NO: " + withdrawApprovalRequestDto.getMTrdNo();
                    MonitAgent.sendMonitAgent(ErrorCode.MACNT_NO_NOT_FOUND.getErrorCode(), msg);
                    throw new RequestValidationException(ErrorCode.MACNT_NO_NOT_FOUND);
                }

                String macBankCd = (String) tgtMacntNo[1];
                String encMacntAccNo = (String) tgtMacntNo[2];
                DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
                String decMacntAccNo = databaseAESCryptoUtil.convertToEntityAttribute(encMacntAccNo);

                log.info("모계좌 계좌번호 암호화: [{}]", encMacntAccNo);
                String mskMacntAccNo = CommonUtil.maskingString(decMacntAccNo, 3, 3);

                //환불예정일자
                log.info("환불예정일자: [{}]", commonService.workingDay(curDt, 1));

                saveTrade = true;

                RfdRcptInsertDto rfdRcptInsertDto = RfdRcptInsertDto.builder()
                        .rfdRcptNo(rfdRcptNo)
                        .mid(rfdMid)
                        .svcCd(MpsApiCd.SVC_CD)
                        .prdtCd(MpsPrdtCd.withdrawal.getPrdtCd())
                        .rcptDt(curDt)
                        .rcptTm(curTm)
                        .orgTrdAmt(totWdAmt)
                        .rfdAmt(totWdAmt)
                        .rfdSchDt(commonService.workingDay(curDt, 1)) //환불예정일자 D+1
                        .rfdAcntBankCd(bankCd)
                        .rfdAcntNoEnc(tgtCustMean.getAesAccountNo())
                        .rfdAcntDprNm(custNm)
                        .rfdAcntNoMsk(mskAccNo)
                        .rfdAcntSumry(custTrdSumry)
                        .rmk(trdDivCd + custNo)
                        .rfdStatCd("0000") //고정값
                        .retryCnt(0)
                        .macntBankCd(macBankCd)
                        .macntNoEnc(encMacntAccNo)
                        .macntNoMsk(mskMacntAccNo)
                        .macntSumry(custNo)
                        .rfdApprStatCd("N") //고정값
                        .build();
                rfdRcptInsertService.insertRfdRcpt(rfdRcptInsertDto);
            }else if(mpsMarket.getWdTypeCd().equals(WdTypeCd.SELF.getWdTypeCd())){
                // wdTypeCd = '03'의 경우 환불거래번호가 없음으로 1
                chrgTrdNo = "1";
                trdSumry = "1";
                saveTrade = true;
            }

            if(saveTrade){
                //정상거래 set
                tradeRepository.save(Trade.builder()
                        .trdNo(trdNo)
                        .trdDt(curDt)
                        .trdTm(curTm)
                        .trdDivCd(trdDivCd)
                        .svcCd(MpsApiCd.SVC_CD)
                        .prdtCd(MpsPrdtCd.withdrawal.getPrdtCd())
                        .mid(mid)
                        .amtSign(-1)
                        .trdAmt(Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()))
                        .mnyAmt(Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()))
                        .pntAmt(0)
                        .waitMnyAmt(waitAmt)
                        .mnyBlc(withdrawalOut.getOutMnyBlc())
                        .pntBlc(withdrawalOut.getOutPntBlc())
                        .waitMnyBlc(withdrawalOut.getOutWaitMnyBlc())
                        .custBdnFeeAmt(custBdnFee)
                        .blcUseOrd(blcUseOrd)
                        .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())//충전수단 코드
                        .mReqDtm(reqDtm)
                        .mTrdNo(withdrawApprovalRequestDto.getMTrdNo())
                        .mpsCustNo(custNo)
                        .trdSumry("bankCd:" + tgtCustMean.getBankCd() + ";accNo:" + custAcNoStr + ";WD:" + trdSumry)
                        .stlMId(mid)
                        .storCd("N")
                        .storNm("N")
                        .mCustId(mCustId)
                        .chrgTrdNo(chrgTrdNo)
                        .mResrvField1(withdrawApprovalRequestDto.getMResrvField1())
                        .mResrvField2(withdrawApprovalRequestDto.getMResrvField2())
                        .mResrvField3(withdrawApprovalRequestDto.getMResrvField3())
                        .createdIp(ServerInfoConfig.HOST_IP)
                        .createdId(ServerInfoConfig.HOST_NAME)
                        .build()
                );
            }

            WithdrawApprovalResponseDto withdrawApprovalResponseDto = WithdrawApprovalResponseDto.builder()
                    .custNo(custNo)
                    .mTrdNo(withdrawApprovalRequestDto.getMTrdNo())
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdAmt(withdrawApprovalRequestDto.getTrdAmt())
                    .mnyBlc(String.valueOf(withdrawalOut.getOutMnyBlc()))
                    .custBdnFeeAmt(String.valueOf(custBdnFee))
                    .build();

            return withdrawApprovalResponseDto;

        } else {
            TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
            tradeFailInsertDto.setTrdNo(trdNo);
            tradeFailInsertDto.setCustNo(custNo);
            tradeFailInsertDto.setFailDt(curDt);
            tradeFailInsertDto.setFailTm(curTm);
            tradeFailInsertDto.setTrdDivCd(trdDivCd);
            tradeFailInsertDto.setMid(mid);
            tradeFailInsertDto.setMCustId(mCustId);
            tradeFailInsertDto.setMTrdNo(withdrawApprovalRequestDto.getMTrdNo());
            tradeFailInsertDto.setAmtSign(-1);
            tradeFailInsertDto.setTrdAmt(Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()));
            tradeFailInsertDto.setMnyAmt(Long.parseLong(withdrawApprovalRequestDto.getTrdAmt()));
            tradeFailInsertDto.setPntAmt(0);
            tradeFailInsertDto.setCustBdnFeeAmt(custBdnFee);
            tradeFailInsertDto.setWaitMnyAmt(0);
            tradeFailInsertDto.setMnyBlc(withdrawalOut.getOutMnyBlc());
            tradeFailInsertDto.setPntBlc(withdrawalOut.getOutPntBlc());
            tradeFailInsertDto.setWaitMnyBlc(withdrawalOut.getOutWaitMnyBlc());
            tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
            tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
            tradeFailInsertDto.setReqDtm(reqDtm);
            tradeFailInsertDto.setTrdSumry("bankCd:" + tgtCustMean.getBankCd() + ";accNo:" + custAcNoStr);
            tradeFailInsertDto.setErrCd(String.valueOf(resCode));
            tradeFailInsertDto.setErrMsg(resMsg);
            tradeFailInsertDto.setPrdtCd(MpsPrdtCd.withdrawal.getPrdtCd());
            tradeFailInsertDto.setChrgTrdNo(chrgTrdNo);
            log.info("머니출금 거래실패 data : {} ", tradeFailInsertDto);
            tradeFailService.insertTradeFail(tradeFailInsertDto);
        }

        if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);

        } else if (resCode == ProcResCd.REQ_AMT_NOT_MATCHED.getResCd()) { //요청금액 > 잔액
            throw new RequestValidationException(ErrorCode.WITHDRAW_AMT_ERROR);

        } else if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);

        } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd() || resCode == ProcResCd.ERROR.getResCd()) {
            log.info("거래실패: {}", resMsg);
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE);
        }else{
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE, " [머니 출금 오류]");
        }
    }

    /* 대기머니 출금 */
    @Transactional(rollbackFor = Exception.class)
    public WaitMnyWithdrawApprovalResponseDto waitMoneyWithdraw(WaitMnyWithdrawApprovalRequestDto waitMnyWithdrawApprovalRequestDto) throws Exception {

        log.info("회원번호: [{}], 상점거래번호: [{}]", waitMnyWithdrawApprovalRequestDto.getCustomerDto().getMpsCustNo(), waitMnyWithdrawApprovalRequestDto.getMTrdNo());

        /* 일자 시각 SET */
        String reqDtm = "";
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();
        if (CommonUtil.nullTrim(waitMnyWithdrawApprovalRequestDto.getReqDt()).equals("")) {
            reqDtm = curDt + curTm;
        } else {
            String reqTm = (waitMnyWithdrawApprovalRequestDto.getReqTm() == null ? curTm : waitMnyWithdrawApprovalRequestDto.getReqTm());
            reqDtm = waitMnyWithdrawApprovalRequestDto.getReqDt() + reqTm;
        }

        String mCustId = waitMnyWithdrawApprovalRequestDto.getCustomerDto().getMCustId();
        String trdNo = "";
        String result = "";
        trdNo = sequenceService.generateTradeSeq01();
        String custNo = waitMnyWithdrawApprovalRequestDto.getCustNo();
        String mid = waitMnyWithdrawApprovalRequestDto.getCustomerDto().getMid();
        String trdDivCd = waitMnyWithdrawApprovalRequestDto.getDivCd();
        String blcUseOrd = TrBlcUseOrd.MONEY.getBlcUseOrd();
        Long waitAmt = 0L;
        String rfdRcptNo;
        String chrgTrdNo = null;
        CustomerDto customerDto = waitMnyWithdrawApprovalRequestDto.getCustomerDto();

        /* 핀번호, 빌키 검증 */
        MpsMarket mpsMarket = mpsMarketRepository.findMpsMarketByMid(customerDto.getMid()).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));

        if ("N".equalsIgnoreCase(mpsMarket.getPinVrifyYn())) {
            //검증안하는 가맹점
            log.info("###### CheckPin [End] => mId:[{}] pinVrifyYn is N", mid);
        } else {
            String pinNo = waitMnyWithdrawApprovalRequestDto.getPinNo();
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
                TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
                tradeFailInsertDto.setTrdNo(trdNo);
                tradeFailInsertDto.setCustNo(custNo);
                tradeFailInsertDto.setFailDt(curDt);
                tradeFailInsertDto.setFailTm(curTm);
                tradeFailInsertDto.setTrdDivCd(trdDivCd);
                tradeFailInsertDto.setMid(mid);
                tradeFailInsertDto.setMCustId(mCustId);
                tradeFailInsertDto.setMTrdNo(waitMnyWithdrawApprovalRequestDto.getMTrdNo());
                tradeFailInsertDto.setAmtSign(-1);
                tradeFailInsertDto.setTrdAmt(Long.parseLong(waitMnyWithdrawApprovalRequestDto.getWaitMnyBlc()));
                tradeFailInsertDto.setMnyAmt(0);
                tradeFailInsertDto.setPntAmt(0);
                tradeFailInsertDto.setWaitMnyAmt(Long.parseLong(waitMnyWithdrawApprovalRequestDto.getWaitMnyBlc()));
                tradeFailInsertDto.setMnyBlc(0);
                tradeFailInsertDto.setPntBlc(0);
                tradeFailInsertDto.setWaitMnyBlc(Long.parseLong(waitMnyWithdrawApprovalRequestDto.getWaitMnyBlc()));
                tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
                tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
                tradeFailInsertDto.setReqDtm(reqDtm);
                tradeFailInsertDto.setTrdSumry("대기머니 출금");
                tradeFailInsertDto.setErrCd(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorCode());
                tradeFailInsertDto.setErrMsg(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorMessage() + chkPinErrorCountResponseDto.getPinVerifyResultMsgForTrdFail());
                tradeFailInsertDto.setPrdtCd(MpsPrdtCd.withdrawal.getPrdtCd());
                log.info("대기머니 출금 거래실패 data : {} ", tradeFailInsertDto);
                tradeFailService.insertTradeFail(tradeFailInsertDto);

                throw new RequestValidationException(ErrorCode.PIN_NOT_MATCHED, chkPinErrorCountResponseDto.getPinVerifyResultMsg());
            }
        }
        /* TB_MPS_M joinType 검증 */
        if(!mpsMarket.getCustJoinTypeCd().equals("1")){
            if (waitMnyWithdrawApprovalRequestDto.getCustomerDto().getCustDivCd().equals(CustDivCd.ANONYMOUS.getCustDivCd())) {
                throw new RequestValidationException(ErrorCode.CUSTOMER_UNINSCRIBED);
            }
        }

        //거래구분코드 검증
        if (!CommonUtil.nullTrim(waitMnyWithdrawApprovalRequestDto.getDivCd()).equals(TrdDivCd.WAITMONEY_WITHDRAW.getTrdDivCd())) {
            throw new RequestValidationException(ErrorCode.TRADE_DIV_CD_ERROR);
        }

        /* 회원 잔액조회 */
        CustomerWalletResponseDto custWallet = walletService.getCustWalletByCustNo(custNo);

        /* 출금가능금액 조회 */
        waitAmt = custWallet.getWaitMnyBlc();
        log.info("대기머니 :[{}]", waitAmt);
        if (waitAmt == 0) {
            throw new RequestValidationException(ErrorCode.WAIT_WITHDRAW_AMT_FAIL);
        }

        if(!String.valueOf(waitAmt).equals(waitMnyWithdrawApprovalRequestDto.getWaitMnyBlc())){
            throw new RequestValidationException(ErrorCode.WAIT_WITHDRAW_ERROR);
        }

        /* 회원 계좌번호 조회 */
        GetCustChrgMeanResponseDto tgtCustMean = commonService.getCustChrgMean(custNo, TrdChrgMeanCd.RESISTERD_ACCOUNT.getChrgMeanCd());
        String custAccNo = tgtCustMean.getAccountNo();
        int strIndex = custAccNo.length();
        String custAcNoStr = custAccNo.substring(strIndex - 3);
        String bankCd = tgtCustMean.getBankCd();
        String custNm = tgtCustMean.getCustNm();
        String mskAccNo = CommonUtil.maskingString(custAccNo, 3, 3);

        CustomerDto tgtCustomer = waitMnyWithdrawApprovalRequestDto.getCustomerDto();

        /* 법인사업자일때 검증 로직 제외 */
        if("C".equals(tgtCustomer.getBizDivCd())){
            /* 사업자번호 검증 */
            if (!tgtCustMean.getBizRegNo().equals(tgtCustomer.getBizRegNo())) {
                throw new RequestValidationException(ErrorCode.WITHDRAW_BIZREGNO_ERROR);
            }
        }else{
            /* 예금주명 검증 */
            if (!CommonUtil.removeAllSpaces(tgtCustMean.getCustNm()).equals(CommonUtil.removeAllSpaces(tgtCustomer.getCustNm()))) {
                throw new RequestValidationException(ErrorCode.WITHDRAW_NAME_ERROR);
            }

            /* 생년월일 검증 */
            if (!tgtCustMean.getBirthDt().equals(tgtCustomer.getBirthDt())) {
                throw new RequestValidationException(ErrorCode.WITHDRAW_BIRTHDT_ERROR);
            }
        }

        /* 고객통장 적요 */
        String custTrdSumry = tgtCustMean.getCustNm();
        if(!CommonUtil.nullTrim(mpsMarket.getWdTrdSumry()).equals("")){
            custTrdSumry = mpsMarket.getWdTrdSumry();
        }
        custTrdSumry = CommonUtil.limitStringLength(custTrdSumry, 7); //전각문자기준 7자리까지

        /* 인출(사용) 프로시저 호출 */
        WithdrawalIn withdrawalIn = WithdrawalIn.builder()
                .inMpsCustNo(custNo)
                .inTrdDivCd(trdDivCd)
                .inBlcUseOrd(blcUseOrd)
                .inUseTrdNo(trdNo)
                .inUseTrdDt(curDt)
                .inTrdAmt(waitAmt)
                .inBlc(waitAmt)
                .inWorkerID(ServerInfoConfig.HOST_NAME)
                .inWorkerIP(ServerInfoConfig.HOST_IP)
                .build();
        WithdrawalOut withdrawalOut = tradeRepository.withdrawal(withdrawalIn);

        long resCode = (long) withdrawalOut.getOutResCd();
        String resMsg = withdrawalOut.getOutResMsg();
        log.info("응답코드: [{}] 응답메시지: [{}]", resCode, resMsg);

        if (resCode == ProcResCd.SUCCESS.getResCd()) {

            boolean saveTrade = true;
            String trdSumry = null;

            if(mpsMarket.getWdTypeCd().equals(WdTypeCd.REAL_TIME.getWdTypeCd())){
                /* 송금 */
                RemittanceApprovalRequestDto remittanceApprovalRequestDto = RemittanceApprovalRequestDto.builder()
                        .mchtId(rfdMid)
                        .mchtTrdNo(trdNo)//선불 주문번호
                        .mchtCustId(waitMnyWithdrawApprovalRequestDto.custNo)
                        .trdDt(curDt)
                        .trdTm(curTm)
                        .bankCd(tgtCustMean.getBankCd())
                        .custAcntNo(custAccNo)
                        .custAcntSumry(custTrdSumry)
                        .trdAmt(waitAmt)
                        .macntSumry(custNo)
                        .build();

                RemittanceResultDto resultDto = firmBankingService.remittanceApproval(remittanceApprovalRequestDto);

                String outStatCd = resultDto.getOutStatCd();
                String outRsltCd = resultDto.getOutRsltCd();
                String outRsltMsg = resultDto.getOutRsltMsg();
                chrgTrdNo = resultDto.getTrdNo();
                trdSumry = outRsltCd;

                if (!outStatCd.equals("0021")) {
                    saveTrade = false;
                    boolean saveFailTrade = false;
                    String message = String.format("**ERROR 발생** 대기머니 출금 / [M_ID: " + mid + ", 선불회원번호: " + custNo + ", 금액: " + waitAmt + ", 응답코드: " + outRsltCd + ", outRsltMsg: " + outRsltMsg + "]");
                    MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);

                    /* VTIM: 타임아웃, ST38: 요청 진행 중, ST04: VAN 요청중 시스템 에러, ST06: 거래번호 정보가 없음 */
                    if (outRsltCd.equals("VTIM") || outRsltCd.equals("ST38") || outRsltCd.equals("ST04") || outRsltCd.equals("ST06")){
                        saveTrade = true;
                        RfdRsltCnfInsertDto rfdRsltCnfInsertDto = RfdRsltCnfInsertDto.builder()
                                .trdNo(trdNo)
                                .trdDt(curDt)
                                .trdTm(curTm)
                                .rfdTrdNo(chrgTrdNo)
                                .mid(mid)
                                .mpsCustNo(custNo)
                                .rsltCnfStatCd(RfdRsltCnfStatCd.RETRY.getRsltCnfStatCd())
                                .trdAmt(waitAmt)
                                .rsltCnfCnt(0)
                                .rmk(outRsltCd)
                                .rfdAcntBankCd(bankCd)
                                .rfdAcntNoEnc(tgtCustMean.getAesAccountNo())
                                .rfdAcntNoMsk(mskAccNo)
                                .rmtDivCd(RmtDivCd.WITHDRAW.getRmtDivCd())
                                .build();
                        rfdRsltCnfInsertService.insertRfdRsltCnf(rfdRsltCnfInsertDto);
                        chrgTrdNo = null;
                    } else {
                        saveFailTrade = true;
                    }

                    if(saveFailTrade){
                        TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
                        tradeFailInsertDto.setTrdNo(trdNo);
                        tradeFailInsertDto.setCustNo(custNo);
                        tradeFailInsertDto.setFailDt(curDt);
                        tradeFailInsertDto.setFailTm(curTm);
                        tradeFailInsertDto.setTrdDivCd(trdDivCd);
                        tradeFailInsertDto.setMid(mid);
                        tradeFailInsertDto.setMCustId(mCustId);
                        tradeFailInsertDto.setMTrdNo(waitMnyWithdrawApprovalRequestDto.getMTrdNo());
                        tradeFailInsertDto.setAmtSign(-1);
                        tradeFailInsertDto.setTrdAmt(waitAmt);
                        tradeFailInsertDto.setMnyAmt(0);
                        tradeFailInsertDto.setPntAmt(0);
                        tradeFailInsertDto.setWaitMnyAmt(waitAmt);
                        tradeFailInsertDto.setMnyBlc(withdrawalOut.getOutMnyBlc());
                        tradeFailInsertDto.setPntBlc(withdrawalOut.getOutPntBlc());
                        tradeFailInsertDto.setWaitMnyBlc(withdrawalOut.getOutWaitMnyBlc());
                        tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
                        tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
                        tradeFailInsertDto.setReqDtm(reqDtm);
                        tradeFailInsertDto.setTrdSumry("bankCd:" + tgtCustMean.getBankCd() + ";accNo:" + custAcNoStr);
                        tradeFailInsertDto.setErrCd(outStatCd);
                        tradeFailInsertDto.setErrMsg("outRsltCd:" + outRsltCd + ", trdNo:" + chrgTrdNo + ", outRsltMsg:" + outRsltMsg);
                        tradeFailInsertDto.setPrdtCd(MpsPrdtCd.withdrawal.getPrdtCd());
                        tradeFailInsertDto.setChrgTrdNo(chrgTrdNo);
                        log.info("대기머니 출금 거래실패 data : {} ", tradeFailInsertDto);
                        tradeFailService.insertTradeFail(tradeFailInsertDto);
                        throw new RequestValidationException(ErrorCode.REMITTANCE_ERROR,outRsltMsg);
                    }
                }
            } else if(mpsMarket.getWdTypeCd().equals(WdTypeCd.BATCH.getWdTypeCd())){
                /* trd.TB_RFD_RCPT 테이블 INSERT */
                rfdRcptNo = sequenceService.generateRfdRcptSeq01();
                trdSumry = rfdRcptNo;
                chrgTrdNo = rfdRcptNo;

                Object[] tgtMacntNo = (Object[]) dpmnNotiRepository.findByMAcntEnc(rfdMid);
                if (tgtMacntNo == null) {
                    String msg = "MPS_CUST_NO: " + custNo + ", 금액: " + waitAmt + "M_TRD_NO: " + waitMnyWithdrawApprovalRequestDto.getMTrdNo();
                    MonitAgent.sendMonitAgent(ErrorCode.MACNT_NO_NOT_FOUND.getErrorCode(), msg);
                    throw new RequestValidationException(ErrorCode.MACNT_NO_NOT_FOUND);
                }

                String macBankCd = (String) tgtMacntNo[1];
                String encMacntAccNo = (String) tgtMacntNo[2];
                DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
                String decMacntAccNo = databaseAESCryptoUtil.convertToEntityAttribute(encMacntAccNo);

                log.info("모계좌 계좌번호 암호화: [{}]", encMacntAccNo);
                String mskMacntAccNo = CommonUtil.maskingString(decMacntAccNo, 3, 3);

                //환불예정일자
                log.info("환불예정일자: [{}]", commonService.workingDay(curDt, 1));
                saveTrade = true;

                RfdRcptInsertDto rfdRcptInsertDto = RfdRcptInsertDto.builder()
                        .rfdRcptNo(rfdRcptNo)
                        .mid(rfdMid)
                        .svcCd(MpsApiCd.SVC_CD)
                        .prdtCd(MpsPrdtCd.withdrawal.getPrdtCd())
                        .rcptDt(curDt)
                        .rcptTm(curTm)
                        .orgTrdAmt(waitAmt)
                        .rfdAmt(waitAmt)
                        .rfdSchDt(commonService.workingDay(curDt, 1)) //환불예정일자 D+1
                        .rfdAcntBankCd(bankCd)
                        .rfdAcntNoEnc(tgtCustMean.getAesAccountNo())
                        .rfdAcntDprNm(custNm)
                        .rfdAcntNoMsk(mskAccNo)
                        .rfdAcntSumry(custTrdSumry)
                        .rmk(trdDivCd + custNo)
                        .rfdStatCd("0000") //고정값
                        .retryCnt(0)
                        .macntBankCd(macBankCd)
                        .macntNoEnc(encMacntAccNo)
                        .macntNoMsk(mskMacntAccNo)
                        .macntSumry(custNo)
                        .rfdApprStatCd("N") //고정값
                        .build();
                rfdRcptInsertService.insertRfdRcpt(rfdRcptInsertDto);
            }else if(mpsMarket.getWdTypeCd().equals(WdTypeCd.SELF.getWdTypeCd())){
                // wdTypeCd = '03'의 경우 환불거래번호가 없음으로 1
                chrgTrdNo = "1";
                trdSumry = "1";
                saveTrade = true;
            }

            if(saveTrade){
                //정상거래 set
                tradeRepository.save(Trade.builder()
                        .trdNo(trdNo)
                        .trdDt(curDt)
                        .trdTm(curTm)
                        .trdDivCd(trdDivCd)
                        .svcCd(MpsApiCd.SVC_CD)
                        .prdtCd(MpsPrdtCd.withdrawal.getPrdtCd())
                        .mid(mid)
                        .mCustId(mCustId)
                        .amtSign(-1)
                        .trdAmt(waitAmt)
                        .mnyAmt(0)
                        .pntAmt(0)
                        .waitMnyAmt(waitAmt)
                        .mnyBlc(withdrawalOut.getOutMnyBlc())
                        .pntBlc(withdrawalOut.getOutPntBlc())
                        .waitMnyBlc(withdrawalOut.getOutWaitMnyBlc())
                        .blcUseOrd(blcUseOrd)
                        .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())//충전수단 코드
                        .mReqDtm(reqDtm)
                        .mTrdNo(waitMnyWithdrawApprovalRequestDto.getMTrdNo())
                        .mpsCustNo(custNo)
                        .trdSumry("bankCd:" + tgtCustMean.getBankCd() + ";accNo:" + custAcNoStr + ";WD:" + trdSumry)
                        .stlMId(mid)
                        .storCd("N")
                        .storNm("N")
                        .chrgTrdNo(chrgTrdNo)
                        .mResrvField1(waitMnyWithdrawApprovalRequestDto.getMResrvField1())
                        .mResrvField2(waitMnyWithdrawApprovalRequestDto.getMResrvField2())
                        .mResrvField3(waitMnyWithdrawApprovalRequestDto.getMResrvField3())
                        .createdIp(ServerInfoConfig.HOST_IP)
                        .createdId(ServerInfoConfig.HOST_NAME)
                        .build()
                );
            }

            WaitMnyWithdrawApprovalResponseDto waitMnyWithdrawApprovalResponseDto = WaitMnyWithdrawApprovalResponseDto.builder()
                    .custNo(custNo)
                    .mTrdNo(waitMnyWithdrawApprovalRequestDto.getMTrdNo())
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdAmt(String.valueOf(waitAmt))
                    .waitMnyBlc(String.valueOf(withdrawalOut.getOutWaitMnyBlc()))
                    .build();

            return waitMnyWithdrawApprovalResponseDto;

        } else {
            TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
            tradeFailInsertDto.setTrdNo(trdNo);
            tradeFailInsertDto.setCustNo(custNo);
            tradeFailInsertDto.setFailDt(curDt);
            tradeFailInsertDto.setFailTm(curTm);
            tradeFailInsertDto.setTrdDivCd(trdDivCd);
            tradeFailInsertDto.setMid(mid);
            tradeFailInsertDto.setMCustId(mCustId);
            tradeFailInsertDto.setMTrdNo(waitMnyWithdrawApprovalRequestDto.getMTrdNo());
            tradeFailInsertDto.setAmtSign(-1);
            tradeFailInsertDto.setTrdAmt(waitAmt);
            tradeFailInsertDto.setMnyAmt(0);
            tradeFailInsertDto.setPntAmt(0);
            tradeFailInsertDto.setWaitMnyAmt(waitAmt);
            tradeFailInsertDto.setMnyBlc(withdrawalOut.getOutMnyBlc());
            tradeFailInsertDto.setPntBlc(withdrawalOut.getOutPntBlc());
            tradeFailInsertDto.setWaitMnyBlc(withdrawalOut.getOutWaitMnyBlc());
            tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
            tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
            tradeFailInsertDto.setReqDtm(reqDtm);
            tradeFailInsertDto.setTrdSumry("bankCd:" + tgtCustMean.getBankCd() + ";accNo:" + custAcNoStr);
            tradeFailInsertDto.setErrCd(String.valueOf(resCode));
            tradeFailInsertDto.setErrMsg(resMsg);
            tradeFailInsertDto.setChrgTrdNo(chrgTrdNo);
            tradeFailInsertDto.setPrdtCd(MpsPrdtCd.withdrawal.getPrdtCd());
            log.info("대기머니 출금 거래실패 data : {} ", tradeFailInsertDto);
            tradeFailService.insertTradeFail(tradeFailInsertDto);
        }

        if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);

        } else if (resCode == ProcResCd.REQ_AMT_NOT_MATCHED.getResCd()) { //요청금액 > 잔액
            throw new RequestValidationException(ErrorCode.WITHDRAW_AMT_ERROR);

        } else if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);

        } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd() || resCode == ProcResCd.ERROR.getResCd()) {
            log.info("거래실패: {}", resMsg);
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE);
        }else{
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE, " [대기머니 출금 오류]");
        }

    }

    /* 머니 선물 */
    @Transactional(rollbackFor = Exception.class)
    public MoneyGiftResponseDto moneyGift(MoneyGiftRequestDto moneyGiftRequestDto) {

        log.info("회원번호: [{}], 상점거래번호: [{}], 수신자 회원번호: [{}], 요청금액: [{}]", moneyGiftRequestDto.getCustomerDto().getMpsCustNo(), moneyGiftRequestDto.getMTrdNo(), moneyGiftRequestDto.getResCustNo(), moneyGiftRequestDto.getTrdAmt());

        String trdNo = "";
        trdNo = sequenceService.generateTradeSeq01();
        String blcUseOrd = TrBlcUseOrd.MONEY.getBlcUseOrd();
        Long giftMnAmt = Long.parseLong(moneyGiftRequestDto.getTrdAmt());
        String mCustId = moneyGiftRequestDto.getCustomerDto().getMCustId();
        CustomerDto customerDto = moneyGiftRequestDto.getCustomerDto();

        /* 일자 시각 SET */
        String reqDtm = "";
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();
        if (CommonUtil.nullTrim(moneyGiftRequestDto.getReqDt()).equals("")) {
            reqDtm = curDt + curTm;
        } else {
            String reqTm = (moneyGiftRequestDto.getReqTm() == null ? curTm : moneyGiftRequestDto.getReqTm());
            reqDtm = moneyGiftRequestDto.getReqDt() + reqTm;
        }

        /* 핀번호, 빌키 검증 */
        String mid = customerDto.getMid();

        MpsMarket mpsMarket = mpsMarketRepository.findMpsMarketByMid(mid).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));
        if ("N".equalsIgnoreCase(mpsMarket.getPinVrifyYn())) {
            //검증안하는 가맹점
            log.info("###### CheckPin [End] => mId:[{}] pinVrifyYn is N", mid);
        } else {
            String pinNo = moneyGiftRequestDto.getPinNo();
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
                TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
                tradeFailInsertDto.setTrdNo(trdNo);
                tradeFailInsertDto.setCustNo(moneyGiftRequestDto.getCustNo());
                tradeFailInsertDto.setFailDt(curDt);
                tradeFailInsertDto.setFailTm(curTm);
                tradeFailInsertDto.setTrdDivCd(TrdDivCd.MONEY_GIFT.getTrdDivCd());
                tradeFailInsertDto.setMid(moneyGiftRequestDto.getCustomerDto().getMid());
                tradeFailInsertDto.setMCustId(mCustId);
                tradeFailInsertDto.setMTrdNo(moneyGiftRequestDto.getMTrdNo());
                tradeFailInsertDto.setAmtSign(-1);
                tradeFailInsertDto.setTrdAmt(giftMnAmt);
                tradeFailInsertDto.setMnyAmt(giftMnAmt);
                tradeFailInsertDto.setPntAmt(0);
                tradeFailInsertDto.setWaitMnyAmt(0);
                tradeFailInsertDto.setMnyBlc(Long.parseLong(moneyGiftRequestDto.getMnyBlc()));
                tradeFailInsertDto.setPntBlc(0);
                tradeFailInsertDto.setWaitMnyBlc(0);
                tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
                tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
                tradeFailInsertDto.setReqDtm(reqDtm);
                tradeFailInsertDto.setRcvMpsCustNo(moneyGiftRequestDto.getResCustNo());
                tradeFailInsertDto.setTrdSumry("머니 선물");
                tradeFailInsertDto.setErrCd(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorCode());
                tradeFailInsertDto.setErrMsg(chkPinErrorCountResponseDto.getPinVerifyResult().getErrorCode().getErrorMessage() + chkPinErrorCountResponseDto.getPinVerifyResultMsgForTrdFail());
                tradeFailInsertDto.setPrdtCd(MpsPrdtCd.use.getPrdtCd());
                log.info("머니선물 거래실패 data : {} ", tradeFailInsertDto);
                tradeFailService.insertTradeFail(tradeFailInsertDto);

                throw new RequestValidationException(ErrorCode.PIN_NOT_MATCHED, chkPinErrorCountResponseDto.getPinVerifyResultMsg());
            }
        }
        /* 수신회원 != 발신회원 검증  */
        if(moneyGiftRequestDto.getCustNo().equals(moneyGiftRequestDto.getResCustNo())){
            throw new RequestValidationException(ErrorCode.MONEY_GIFT_CUSTNO_ERROR);
        }
        /* 수신회원 상태 검증 */
        CustomerDto resCustomerDto = commonService.getCustomerByCustNo(moneyGiftRequestDto.getResCustNo());
        if (!resCustomerDto.getStatCd().equals(CustStatCd.STANDARD.getStatCd())) {
            throw new RequestValidationException(ErrorCode.RES_CUSTOMER_STATUS_NOT_VALID);
        }
        /* 수신자 상점 검증 */
        if (!resCustomerDto.getMid().equals(moneyGiftRequestDto.getCustomerDto().getMid())) {
            throw new RequestValidationException(ErrorCode.MONEY_GIFT_MARKET_NOT_MATCHED);
        }
        /* 구분코드 검증 */
        if (!CommonUtil.nullTrim(moneyGiftRequestDto.getDivCd()).equals(TrdDivCd.MONEY_GIFT.getTrdDivCd())) {
            throw new RequestValidationException(ErrorCode.TRADE_DIV_CD_ERROR);
        }
        /* 금액검증 */
        if (giftMnAmt <= 0) {
            throw new RequestValidationException(ErrorCode.TRADE_AMT_ERROR);
        }

        //2026-02-10 무기명 회원 머니선물 제한 제거 => ITS: PREPAY-1177
//        /* 기명검증 */
//        if (!moneyGiftRequestDto.getCustomerDto().getCustDivCd().equals(CustDivCd.NAMED.getCustDivCd())) {
//            throw new RequestValidationException(ErrorCode.CUSTOMER_UNINSCRIBED);
//        }
//        if (!resCustomerDto.getCustDivCd().equals(CustDivCd.NAMED.getCustDivCd())) {
//            throw new RequestValidationException(ErrorCode.RES_CUSTOMER_CUSTOMER_UNINSCRIBED);
//        }
        /* 발신자 잔액조회 */
        CustomerWalletResponseDto custWallet = walletService.getCustWalletByCustNo(moneyGiftRequestDto.getCustNo());
        if (custWallet.getMnyBlc() < giftMnAmt) {
            throw new RequestValidationException(ErrorCode.MONEY_GIFT_AMT_ERROR);
        }
//        if (custWallet.getMnyBlc() != Long.parseLong(moneyGiftRequestDto.getMnyBlc())) {
//            throw new RequestValidationException(ErrorCode.SUCCESS.BALANCE_NOT_MATCHED);
//        }
        /* 수신자 잔액조회 */
        CustomerWalletResponseDto resCustWallet = walletService.getCustWalletByCustNo(resCustomerDto.getMpsCustNo());
        if (resCustWallet.getMnyBlc() + giftMnAmt > resCustomerDto.getChrgLmtAmlt()) {
            throw new RequestValidationException(ErrorCode.MONEY_GIFT_LIMIT_REACHED);
        }

        //프로시저 호출을 위해 In변수 담는 객체 Build
        UseEachIn useEachInParam = UseEachIn.builder()
                .inMpsCustNo(moneyGiftRequestDto.getCustNo())
                .inTrdDivCd(TrdDivCd.COMMON_USE.getTrdDivCd())
                .inUseTrdNo(trdNo)
                .inUseTrdDt(curDt)
                .inMnyAmt(giftMnAmt)
                .inPntAmt(0L)
                .inBlc(Long.parseLong(moneyGiftRequestDto.getMnyBlc()) + custWallet.getPntBlc())
                .inWorkerID(ServerInfoConfig.HOST_NAME)
                .inWorkerIP(ServerInfoConfig.HOST_IP)
                .build();
        UseEachOut useEachOut = tradeRepository.useEach(useEachInParam);

        long resCode = useEachOut.getOutResCd();
        String resMsg = useEachOut.getOutResMsg();
        log.info("선불금 각각 사용(머니 선물) EXEC => 회원번호:[{}] 결과코드:[{}] 결과메세지:[{}]", moneyGiftRequestDto.getCustNo(), resCode, resMsg);

        if (resCode == ProcResCd.SUCCESS.getResCd()) {

            try{
                /* 머니 선물 거래 */
                tradeRepository.save(Trade.builder()
                        .trdNo(trdNo)
                        .trdDt(curDt)
                        .trdTm(curTm)
                        .trdDivCd(TrdDivCd.MONEY_GIFT.getTrdDivCd())
                        .svcCd(MpsApiCd.SVC_CD)
                        .mCustId(mCustId)
                        .prdtCd(MpsPrdtCd.use.getPrdtCd())
                        .mid(moneyGiftRequestDto.getCustomerDto().getMid())
                        .amtSign(-1)
                        .trdAmt(giftMnAmt)
                        .mnyAmt(giftMnAmt)
                        .pntAmt(0)
                        .waitMnyAmt(0)
                        .mnyBlc(useEachOut.getOutMnyBlc())
                        .pntBlc(useEachOut.getOutPntBlc())
                        .waitMnyBlc(useEachOut.getOutWaitMnyBlc())
                        .custBdnFeeAmt(0)
                        .blcUseOrd(blcUseOrd)
                        .csrcIssReqYn("N")
                        .cnclYn("N")
                        .storCd("N")
                        .storNm("N")
                        .stlMId(moneyGiftRequestDto.customerDto.getMid())
                        .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                        .mReqDtm(reqDtm)
                        .mTrdNo(moneyGiftRequestDto.getMTrdNo())
                        .mpsCustNo(moneyGiftRequestDto.getCustNo())
                        .rcvMpsCustNo(moneyGiftRequestDto.getResCustNo())
                        .trdSumry("")
                        .mResrvField1(moneyGiftRequestDto.getMResrvField1())
                        .mResrvField2(moneyGiftRequestDto.getMResrvField2())
                        .mResrvField3(moneyGiftRequestDto.getMResrvField3())
                        .createdIp(ServerInfoConfig.HOST_IP)
                        .createdId(ServerInfoConfig.HOST_NAME)
                        .build());

                /* 머니 받기 */
                MoneyGiftReceiveRequestDto moneyGiftReceiveRequestDto = MoneyGiftReceiveRequestDto.builder()
                        .mTrdNo(moneyGiftRequestDto.getMTrdNo())
                        .trdNo(trdNo)
                        .trdAmt(giftMnAmt)
                        .trdDt(curDt)
                        .trdTm(curTm)
                        .custDto(moneyGiftRequestDto.getCustomerDto())
                        .resCustDto(resCustomerDto)
                        .resCustWallet(resCustWallet)
                        .build();
                boolean receiveResult = moneyReceive(moneyGiftReceiveRequestDto);
            }catch (Exception e){

                TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
                tradeFailInsertDto.setTrdNo(trdNo);
                tradeFailInsertDto.setCustNo(moneyGiftRequestDto.getCustNo());
                tradeFailInsertDto.setFailDt(curDt);
                tradeFailInsertDto.setFailTm(curTm);
                tradeFailInsertDto.setMCustId(mCustId);
                tradeFailInsertDto.setTrdDivCd(TrdDivCd.MONEY_GIFT.getTrdDivCd());
                tradeFailInsertDto.setMid(moneyGiftRequestDto.getCustomerDto().getMid());
                tradeFailInsertDto.setMTrdNo(moneyGiftRequestDto.getMTrdNo());
                tradeFailInsertDto.setAmtSign(-1);
                tradeFailInsertDto.setTrdAmt(giftMnAmt);
                tradeFailInsertDto.setMnyAmt(giftMnAmt);
                tradeFailInsertDto.setPntAmt(0);
                tradeFailInsertDto.setCustBdnFeeAmt(0);
                tradeFailInsertDto.setWaitMnyAmt(0);
                tradeFailInsertDto.setMnyBlc(Long.parseLong(moneyGiftRequestDto.getMnyBlc()));
                tradeFailInsertDto.setPntBlc(0);
                tradeFailInsertDto.setWaitMnyBlc(0);
                tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
                tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
                tradeFailInsertDto.setReqDtm(reqDtm);
                tradeFailInsertDto.setRcvMpsCustNo(moneyGiftRequestDto.getResCustNo());
                tradeFailInsertDto.setTrdSumry("머니 선물");
                tradeFailInsertDto.setErrCd(ErrorCode.fromErrorMessage(e.getMessage()).getErrorCode());
                tradeFailInsertDto.setErrMsg(ErrorCode.fromErrorMessage(e.getMessage()).getErrorMessage());
                tradeFailInsertDto.setPrdtCd(MpsPrdtCd.use.getPrdtCd());
                log.info("머니받기 거래 중 거래실패 data : {} ", tradeFailInsertDto);
                tradeFailService.insertTradeFail(tradeFailInsertDto);

                throw new RequestValidationException(ErrorCode.MONEY_GIFT_FAIL, ErrorCode.fromErrorMessage(e.getMessage()).getErrorMessage());
            }

            MoneyGiftResponseDto moneyGiftResponseDto = MoneyGiftResponseDto.builder()
                    .custNo(moneyGiftRequestDto.getCustNo())
                    .mTrdNo(moneyGiftRequestDto.getMTrdNo())
                    .resCustNo(moneyGiftRequestDto.getResCustNo())
                    .trdNo(trdNo)
                    .trdAmt(String.valueOf(giftMnAmt))
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .mnyBlc(String.valueOf(useEachOut.getOutMnyBlc()))
                    .build();
            return moneyGiftResponseDto;
        }
        if (resCode != ProcResCd.SUCCESS.getResCd()) {
            TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
            tradeFailInsertDto.setTrdNo(trdNo);
            tradeFailInsertDto.setCustNo(moneyGiftRequestDto.getCustNo());
            tradeFailInsertDto.setFailDt(curDt);
            tradeFailInsertDto.setFailTm(curTm);
            tradeFailInsertDto.setMCustId(mCustId);
            tradeFailInsertDto.setTrdDivCd(TrdDivCd.MONEY_GIFT.getTrdDivCd());
            tradeFailInsertDto.setMid(moneyGiftRequestDto.getCustomerDto().getMid());
            tradeFailInsertDto.setMTrdNo(moneyGiftRequestDto.getMTrdNo());
            tradeFailInsertDto.setAmtSign(-1);
            tradeFailInsertDto.setTrdAmt(giftMnAmt);
            tradeFailInsertDto.setMnyAmt(giftMnAmt);
            tradeFailInsertDto.setPntAmt(0);
            tradeFailInsertDto.setCustBdnFeeAmt(0);
            tradeFailInsertDto.setWaitMnyAmt(0);
            tradeFailInsertDto.setMnyBlc(Long.parseLong(moneyGiftRequestDto.getMnyBlc()));
            tradeFailInsertDto.setPntBlc(0);
            tradeFailInsertDto.setWaitMnyBlc(0);
            tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
            tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
            tradeFailInsertDto.setReqDtm(reqDtm);
            tradeFailInsertDto.setRcvMpsCustNo(moneyGiftRequestDto.getResCustNo());
            tradeFailInsertDto.setTrdSumry("머니 선물");
            tradeFailInsertDto.setErrCd(String.valueOf(resCode));
            tradeFailInsertDto.setErrMsg(resMsg);
            tradeFailInsertDto.setPrdtCd(MpsPrdtCd.use.getPrdtCd());
            log.info("머니선물 거래실패 data : {} ", tradeFailInsertDto);
            tradeFailService.insertTradeFail(tradeFailInsertDto);
        }
        if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);

        } else if (resCode == ProcResCd.REQ_AMT_NOT_MATCHED.getResCd()) { //요청금액 > 잔액
            throw new RequestValidationException(ErrorCode.REQ_AMT_NOT_MATCHED);

        }  else if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);

        } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd() || resCode == ProcResCd.ERROR.getResCd()) {
            log.info("거래실패: {}", resMsg);
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE);
        }else{
            throw new RequestValidationException(ErrorCode.MONEY_GIFT_FAIL);
        }
    }

    /* 머니 받기 */
    @Transactional(rollbackFor = Exception.class)
    public boolean moneyReceive(MoneyGiftReceiveRequestDto moneyGiftReceiveRequestDto) {
        String trdNo = sequenceService.generateTradeSeq01();
        String mCustId = moneyGiftReceiveRequestDto.getCustDto().getMCustId();

        //수신자 거래내역 SET
        CustomerDto resCustDto = moneyGiftReceiveRequestDto.getResCustDto();
        CustomerWalletResponseDto resCustomerWalletResponseDto = moneyGiftReceiveRequestDto.getResCustWallet();

        boolean result = false;

        /* 프로시저 호출 */
        PayIn payIn = PayIn.builder()
                .inMpsCustNo(resCustDto.getMpsCustNo())
                .inChrgMeanCd(TrdChrgMeanCd.MONEY_RECEIVE.getChrgMeanCd())
                .inTrdDivCd(TrdDivCd.MONEY_PROVIDE.getTrdDivCd())
                .inTrdDivDtlCd("")
                .inCustDivCd(resCustDto.getCustDivCd())
                .inChrgLmtAmt(resCustDto.getChrgLmtAmlt())
                .inMID(resCustDto.getMid())
                .inPayTrdNo(trdNo)
                .inPayTrdDt(moneyGiftReceiveRequestDto.getTrdDt())
                .inTrdAmt(moneyGiftReceiveRequestDto.getTrdAmt())
                .inBlc(resCustomerWalletResponseDto.getMnyBlc())
                .inVldPd("")
                .inPntId("")
                .inPayRsn(moneyGiftReceiveRequestDto.getCustDto().getMpsCustNo())
                .inWorkerID(ServerInfoConfig.HOST_NAME)
                .inWorkerIP(ServerInfoConfig.HOST_IP)
                .build();
        PayOut payOut = tradeRepository.doPay(payIn);

        long resCode = (long) payOut.getOutResCd();
        String resMsg = payOut.getOutResMsg();
        log.info("응답코드: [{}] 응답메시지: [{}]", resCode, resMsg);

        if (resCode == ProcResCd.SUCCESS.getResCd()) {

            /* 고객 잔액 SET */
            long mnyBlc = (Long) payOut.getOutMnyBlc();
            long pntBlc = (Long) payOut.getOutPntBlc();
            long waitMnyBlc = (Long) payOut.getOutWaitMnyBlc();
            log.info("머니: [{}] 포인트: [{}], 대기머니: [{}]", mnyBlc, pntBlc, waitMnyBlc);
            tradeRepository.save(Trade.builder()
                    .trdNo(trdNo)
                    .trdDt(moneyGiftReceiveRequestDto.getTrdDt())
                    .trdTm(moneyGiftReceiveRequestDto.getTrdTm())
                    .trdDivCd(TrdDivCd.MONEY_RECEIVE.getTrdDivCd())
                    .svcCd(MpsApiCd.SVC_CD)
                    .prdtCd(MpsPrdtCd.charge.getPrdtCd())
                    .mid(resCustDto.getMid())
                    .amtSign(+1)
                    .trdAmt(moneyGiftReceiveRequestDto.getTrdAmt())
                    .mnyAmt(moneyGiftReceiveRequestDto.getTrdAmt())
                    .pntAmt(0)
                    .waitMnyAmt(0)
                    .mnyBlc(mnyBlc)
                    .pntBlc(pntBlc)
                    .waitMnyBlc(waitMnyBlc)
                    .custBdnFeeAmt(0)
                    .blcUseOrd("M")
                    .stlMId(moneyGiftReceiveRequestDto.getResCustDto().getMid())
                    .storCd("N")
                    .storNm("N")
                    .mCustId(mCustId)
                    .chrgMeanCd(TrdChrgMeanCd.MONEY_RECEIVE.getChrgMeanCd())
                    .mReqDtm(moneyGiftReceiveRequestDto.getTrdDt() + moneyGiftReceiveRequestDto.getTrdTm())
                    .mTrdNo(moneyGiftReceiveRequestDto.getMTrdNo())
                    .mpsCustNo(resCustDto.getMpsCustNo())
                    .trdSumry(moneyGiftReceiveRequestDto.getCustDto().getMpsCustNo())
                    .createdIp(ServerInfoConfig.HOST_IP)
                    .createdId(ServerInfoConfig.HOST_NAME)
                    .build());

            result = true;

        } else {

            ErrorCode errorCode = ErrorCode.SERVER_ERROR_CODE;
            if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
                throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);
            } else if (resCode == ProcResCd.LIMIT_REACHED.getResCd()) {
                throw new RequestValidationException(ErrorCode.CHARGE_LIMIT_REACHED);
            } else if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
                throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);
            } else if (resCode == ProcResCd.MONTHLY_LIMIT_REACHED.getResCd()) {
                throw new RequestValidationException(ErrorCode.MONTHLY_LIMIT_REACHED);
            } else if (resCode == ProcResCd.RETRY_NEEDED.getResCd() || resCode == ProcResCd.ERROR.getResCd()) {
                throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE);
            }
            throw new RequestValidationException(errorCode);
        }
        return result;
    }

    /* 출금예정 여부 조회 */
    public WillMnyWithdrawalYnResponseDto willMnyWithdrawalYn(WillMnyWithdrawalYnRequestDto willMnyWithdrawalYnRequestDto){

        WillMnyWithdrawalYnResponseDto willMnyWithdrawalYnResponseDto = new WillMnyWithdrawalYnResponseDto();
        boolean result = true;

        List<String> trdDivCdList = new ArrayList<>(Arrays.asList(TrdDivCd.MONEY_WITHDRAW.getTrdDivCd(), TrdDivCd.TERMINATE_WITHDRAW.getTrdDivCd(), TrdDivCd.WAITMONEY_WITHDRAW.getTrdDivCd()));
        List<Trade> tgtTrade = tradeRepository.findByMpsCustNoAndTrdDivCdIn(willMnyWithdrawalYnRequestDto.getCustNo(), trdDivCdList);

        if (tgtTrade.size() > 0) {
            for (Trade list : tgtTrade) {
                result = validWdYn(list);
                if (!result) {
                    log.error("환불예정거래건 거래번호: [{}]", list.getTrdNo());
                    break;
                }
            }
        }

        if(result){ //환불거래 없을 때
            willMnyWithdrawalYnResponseDto.saveWdMny(willMnyWithdrawalYnRequestDto.getCustNo(), willMnyWithdrawalYnRequestDto.getCustomerDto().getMid(), "N");
        }else{
            willMnyWithdrawalYnResponseDto.saveWdMny(willMnyWithdrawalYnRequestDto.getCustNo(), willMnyWithdrawalYnRequestDto.getCustomerDto().getMid(), "Y");
        }

        return willMnyWithdrawalYnResponseDto;
    }

    /**
     *
     * @param trade
     * @return
     * wdMnyYn -> true : 출금예정거래 없음
     *         -> false : 출금예정거래 있음
     */
    public boolean validWdYn(Trade trade){

        boolean wdMnyYn = true;
        RfdRcpt tgtRfdRcpt;
        String wdAmt;

        /* 출금 신청 금액 */
        wdAmt = String.valueOf(trade.getMnyAmt() - trade.getCustBdnFeeAmt());
        if (trade.getTrdDivCd().equals(TrdDivCd.WAITMONEY_WITHDRAW.getTrdDivCd())) {
            wdAmt = String.valueOf(trade.getWaitMnyAmt());
        }
        if (trade.getChrgTrdNo().startsWith("RF")) {//배치환불
            tgtRfdRcpt = rfdRcptRepository.findByRfdRcptNoAndMidAndSvcCdAndRcptDtAndRfdAmt(trade.getChrgTrdNo(), rfdMid, MpsApiCd.SVC_CD, trade.getTrdDt(), Long.parseLong(wdAmt));
            if (tgtRfdRcpt == null) {
                log.error("[환불거래접수정보 조회 오류] 거래번호: [{}]", trade.getTrdNo());
                throw new RequestValidationException(ErrorCode.RFD_RCPT_NOT_FOUND);
            } else {
                if (!tgtRfdRcpt.getRfdStatCd().equals("0021")) {
                    log.error("[배치환불 실패거래] 거래번호: [{}]", trade.getTrdNo());
                    return false;
                }
            }
        }
        return wdMnyYn;
    }

    /* 백오피스 환불 재처리 API */
    public RetryMoneyWithdrawResponseDto retryMoneyWithdraw(RetryMoneyWithdrawRequestDto retryMoneyWithdrawRequestDto) throws Exception {

        String outStatCd = null;
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();

        /* 환불재처리 원장 검증 실제 F 인지 */
        RfdRsltCnf tgtRsltCnf = rfdRsltCnfRepository.findByRfdTrdNoAndTrdNoAndTrdDt(retryMoneyWithdrawRequestDto.rfdTrdNo, retryMoneyWithdrawRequestDto.orgTrdNo, retryMoneyWithdrawRequestDto.getOrgTrdDt(), retryMoneyWithdrawRequestDto.getCustNo());

        if(tgtRsltCnf == null || tgtRsltCnf.getReprocRfdStatCd().equals(RfdRsltCnfStatCd.SUCCESS.getRsltCnfStatCd())){
            throw new RequestValidationException(ErrorCode.RETRY_MONEY_WITHDRAW_NOT_MATCHED);
        }

        if(tgtRsltCnf.getRsltCnfStatCd().equals(RfdRsltCnfStatCd.FAIL.getRsltCnfStatCd())){
            /* 실시간환불(송금) */
            CustomerDto tgtCustomer = commonService.getCustomerByCustNo(tgtRsltCnf.getMpsCustNo()); //출금할당시에는 정상고객이었으므로 검증 제외
            DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
            String decAccountNo = databaseAESCryptoUtil.convertToEntityAttribute(tgtRsltCnf.getRfdAcntNoEnc());

            RemittanceApprovalRequestDto remittanceApprovalRequestDto = RemittanceApprovalRequestDto.builder()
                    .mchtId(tgtCustomer.getMid())
                    .mchtTrdNo(tgtRsltCnf.getTrdNo())//실패한 주문번호
                    .mchtCustId(tgtCustomer.getMpsCustNo())
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .bankCd(tgtRsltCnf.getRfdAcntBankCd())
                    .custAcntNo(decAccountNo)
                    .custAcntSumry(tgtCustomer.getCustNm())
                    .trdAmt(tgtRsltCnf.getTrdAmt())
                    .macntSumry(tgtCustomer.getMpsCustNo())
                    .build();

            RemittanceResultDto resultDto = firmBankingService.remittanceApproval(remittanceApprovalRequestDto);

            outStatCd = resultDto.getOutStatCd();
            String outRsltCd = resultDto.getOutRsltCd();
            String chrgTrdNo = resultDto.getTrdNo();
            if (outStatCd.equals("0021")) {

                /* 원거래 조회 */
                Trade orgTrade = tradeRepository.findTradeByMpsCustNoAndTrdNoAndTrdDt(tgtRsltCnf.getMpsCustNo(), tgtRsltCnf.getTrdNo(), tgtRsltCnf.getTrdDt())
                        .orElseThrow(() -> new RequestValidationException(ErrorCode.TRADE_ORIGINAL_NOT_FOUND));

                /* 거래구분코드 검증 */
                if(!orgTrade.getTrdDivCd().equals(TrdDivCd.MONEY_WITHDRAW.getTrdDivCd()) && !orgTrade.getTrdDivCd().equals(TrdDivCd.WAITMONEY_WITHDRAW.getTrdDivCd()) && !orgTrade.getTrdDivCd().equals(TrdDivCd.TERMINATE_WITHDRAW.getTrdDivCd())){
                    throw new RequestValidationException(ErrorCode.TRADE_ORIGINAL_NOT_FOUND);
                }

                /* 원거래 업데이트 */
                orgTrade.retryWdTradeUpdate(chrgTrdNo);
                tradeRepository.save(orgTrade);
                tgtRsltCnf.saveRetryTrade(RfdRsltCnfStatCd.SUCCESS.getRsltCnfStatCd(), outRsltCd, chrgTrdNo);
                rfdRsltCnfRepository.save(tgtRsltCnf);

            }else{
                //실패
                /* VTIM: 타임아웃, ST38: 요청 진행 중, ST04: VAN 요청중 시스템 에러, ST06: 거래번호 정보가 없음 */
                if (outRsltCd.equals("VTIM") || outRsltCd.equals("ST38") || outRsltCd.equals("ST04") || outRsltCd.equals("ST06")){
                    tgtRsltCnf.saveRetryTrade(RfdRsltCnfStatCd.RETRY.getRsltCnfStatCd(), outRsltCd, chrgTrdNo);
                    rfdRsltCnfRepository.save(tgtRsltCnf);

                }else{
                    tgtRsltCnf.saveRetryTrade(RfdRsltCnfStatCd.FAIL.getRsltCnfStatCd(), outRsltCd, chrgTrdNo);
                    rfdRsltCnfRepository.save(tgtRsltCnf);

                    String message = String.format("**ERROR 발생** 출금 재처리 응답 실패/ [선불회원번호: " + tgtRsltCnf.getMpsCustNo() + ", 금액: " + tgtRsltCnf.getTrdAmt() + ", 응답코드: " + outRsltCd + "]");
                    MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
                }

                String message = String.format("**ERROR 발생** 출금 재처리 / [선불회원번호: " + tgtRsltCnf.getMpsCustNo() + ", 금액: " + tgtRsltCnf.getTrdAmt() + ", 응답코드: " + outRsltCd + "]");
                MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), message);
                throw new RequestValidationException(ErrorCode.REMITTANCE_ERROR);
            }
        }

        return RetryMoneyWithdrawResponseDto.builder()
                .orgTrdNo(tgtRsltCnf.getTrdNo())
                .outStatCd(outStatCd)
                .build();
    }

    //관리자 출금
    public AdminWithdrawApprovalResponseDto adminWithdraw(AdminWithdrawApprovalRequestDto adminWithdrawApprovalRequestDto){

        /* 일자 시각 SET */
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();

        String reqDtm = curDt + curTm;
        if (StringUtils.isNotEmpty(adminWithdrawApprovalRequestDto.getReqDt()) && StringUtils.isNotEmpty(adminWithdrawApprovalRequestDto.getReqTm())) {
            reqDtm = adminWithdrawApprovalRequestDto.getReqDt() + adminWithdrawApprovalRequestDto.getReqTm();
        }

        //회원 추출
        String trdNo = "";
        String blcUseOrd = "";
        long inBlc = 0;
        long trdAmt = Long.parseLong(adminWithdrawApprovalRequestDto.getTrdAmt());
        long mnyAmt = 0;
        long waitMnyAmt = 0;
        CustomerDto customerDto = adminWithdrawApprovalRequestDto.getCustomerDto();
        String mCustId = customerDto.getMCustId();
        trdNo = sequenceService.generateTradeSeq01();
        String mid = customerDto.getMid();
        String trdDivCd = CommonUtil.nullTrim(adminWithdrawApprovalRequestDto.getDivCd());

        if(!TrdDivCd.MONEY_WITHDRAW.getTrdDivCd().equals(trdDivCd) && !TrdDivCd.WAITMONEY_WITHDRAW.getTrdDivCd().equals(trdDivCd)){
            throw new RequestValidationException(ErrorCode.ADMIN_WITHDRAWAL_DIV_CD_ERROR);
        }

        //금액검증
        if (trdAmt == 0) {
            throw new RequestValidationException(ErrorCode.TRADE_AMT_ERROR);
        }

        /* 잔액조회 */
        CustomerWalletResponseDto custWallet = walletService.getCustWalletByCustNo(customerDto.getMpsCustNo());
        if(TrdDivCd.MONEY_WITHDRAW.getTrdDivCd().equals(trdDivCd)){
            blcUseOrd = "M";
            mnyAmt = trdAmt;
            inBlc = Long.parseLong(adminWithdrawApprovalRequestDto.getBlcAmt()) + custWallet.getPntBlc();
            trdDivCd = TrdDivCd.ADMIN_WITHDRAW.getTrdDivCd();
            if(custWallet.getMnyBlc() != Long.parseLong(adminWithdrawApprovalRequestDto.getBlcAmt())){
                throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);
            }
            if (custWallet.getMnyBlc() < trdAmt) {
                throw new RequestValidationException(ErrorCode.WITHDRAW_AMT_ERROR);
            }
        }else if(TrdDivCd.WAITMONEY_WITHDRAW.getTrdDivCd().equals(trdDivCd)){
            blcUseOrd = "W";
            waitMnyAmt = trdAmt;
            inBlc = Long.parseLong(adminWithdrawApprovalRequestDto.getBlcAmt());
            trdDivCd = TrdDivCd.WAITMONEY_WITHDRAW.getTrdDivCd();
            if(custWallet.getWaitMnyBlc() != Long.parseLong(adminWithdrawApprovalRequestDto.getBlcAmt())){
                throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);
            }
            if (custWallet.getWaitMnyBlc() < trdAmt) {
                throw new RequestValidationException(ErrorCode.WITHDRAW_AMT_ERROR);
            }
            if(custWallet.getWaitMnyBlc() != trdAmt || Long.parseLong(adminWithdrawApprovalRequestDto.getBlcAmt()) != trdAmt){
                throw new RequestValidationException(ErrorCode.WAIT_WITHDRAW_ERROR);
            }
        }

        /* 인출(사용) 프로시저 호출 */
        WithdrawalIn withdrawalIn = WithdrawalIn.builder()
                .inMpsCustNo(customerDto.getMpsCustNo())
                .inTrdDivCd(trdDivCd)
                .inBlcUseOrd(blcUseOrd)
                .inUseTrdNo(trdNo)
                .inUseTrdDt(curDt)
                .inTrdAmt(trdAmt)
                .inBlc(inBlc)
                .inWorkerID(ServerInfoConfig.HOST_NAME)
                .inWorkerIP(ServerInfoConfig.HOST_IP)
                .build();
        WithdrawalOut withdrawalOut = tradeRepository.withdrawal(withdrawalIn);

        long resCode = (long) withdrawalOut.getOutResCd();
        String resMsg = withdrawalOut.getOutResMsg();
        log.info("응답코드: [{}] 응답메시지: [{}]", resCode, resMsg);

        if (resCode == ProcResCd.SUCCESS.getResCd()) {

            //정상거래 set
            tradeRepository.save(Trade.builder()
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdDivCd(TrdDivCd.ADMIN_WITHDRAW.getTrdDivCd())
                    .svcCd(MpsApiCd.SVC_CD)
                    .prdtCd(MpsPrdtCd.withdrawal.getPrdtCd())
                    .mid(mid)
                    .amtSign(-1)
                    .trdAmt(trdAmt)
                    .mnyAmt(mnyAmt)
                    .pntAmt(0)
                    .waitMnyAmt(waitMnyAmt)
                    .mnyBlc(withdrawalOut.getOutMnyBlc())
                    .pntBlc(withdrawalOut.getOutPntBlc())
                    .waitMnyBlc(withdrawalOut.getOutWaitMnyBlc())
                    .custBdnFeeAmt(0)
                    .blcUseOrd(blcUseOrd)
                    .chrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd())
                    .mReqDtm(reqDtm)
                    .mTrdNo(adminWithdrawApprovalRequestDto.getMTrdNo())
                    .mpsCustNo(customerDto.getMpsCustNo())
                    .trdSumry(adminWithdrawApprovalRequestDto.getTrdSumry())
                    .stlMId(mid)
                    .storCd("N")
                    .storNm("N")
                    .mCustId(mCustId)
                    .mResrvField1(adminWithdrawApprovalRequestDto.getMResrvField1())
                    .mResrvField2(adminWithdrawApprovalRequestDto.getMResrvField2())
                    .mResrvField3(adminWithdrawApprovalRequestDto.getMResrvField3())
                    .createdIp(ServerInfoConfig.HOST_IP)
                    .createdId(ServerInfoConfig.HOST_NAME)
                    .build()
            );

            return AdminWithdrawApprovalResponseDto.builder()
                    .custNo(customerDto.getMpsCustNo())
                    .mTrdNo(adminWithdrawApprovalRequestDto.getMTrdNo())
                    .trdNo(trdNo)
                    .trdDt(curDt)
                    .trdTm(curTm)
                    .trdAmt(adminWithdrawApprovalRequestDto.getTrdAmt())
                    .blcAmt(String.valueOf(withdrawalOut.getOutMnyBlc()))
                    .build();

        } else {
            TradeFailInsertDto tradeFailInsertDto = new TradeFailInsertDto();
            tradeFailInsertDto.setTrdNo(trdNo);
            tradeFailInsertDto.setCustNo(customerDto.getMpsCustNo());
            tradeFailInsertDto.setFailDt(curDt);
            tradeFailInsertDto.setFailTm(curTm);
            tradeFailInsertDto.setTrdDivCd(TrdDivCd.ADMIN_WITHDRAW.getTrdDivCd());
            tradeFailInsertDto.setMid(mid);
            tradeFailInsertDto.setMCustId(mCustId);
            tradeFailInsertDto.setMTrdNo(adminWithdrawApprovalRequestDto.getMTrdNo());
            tradeFailInsertDto.setAmtSign(-1);
            tradeFailInsertDto.setTrdAmt(trdAmt);
            tradeFailInsertDto.setMnyAmt(mnyAmt);
            tradeFailInsertDto.setPntAmt(0);
            tradeFailInsertDto.setCustBdnFeeAmt(0);
            tradeFailInsertDto.setWaitMnyAmt(waitMnyAmt);
            tradeFailInsertDto.setMnyBlc(withdrawalOut.getOutMnyBlc());
            tradeFailInsertDto.setPntBlc(withdrawalOut.getOutPntBlc());
            tradeFailInsertDto.setWaitMnyBlc(withdrawalOut.getOutWaitMnyBlc());
            tradeFailInsertDto.setBlcUseOrd(blcUseOrd);
            tradeFailInsertDto.setChrgMeanCd(TrdChrgMeanCd.ETC.getChrgMeanCd());
            tradeFailInsertDto.setReqDtm(reqDtm);
            tradeFailInsertDto.setTrdSumry(adminWithdrawApprovalRequestDto.getTrdSumry());
            tradeFailInsertDto.setErrCd(String.valueOf(resCode));
            tradeFailInsertDto.setErrMsg(resMsg);
            tradeFailInsertDto.setPrdtCd(MpsPrdtCd.withdrawal.getPrdtCd());
            log.info("관리자출금 거래실패 data : {} ", tradeFailInsertDto);
            tradeFailService.insertTradeFail(tradeFailInsertDto);
        }

        if (resCode == ProcResCd.BALANCE_NOT_MATCHED.getResCd()) {
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);

        } else if (resCode == ProcResCd.REQ_AMT_NOT_MATCHED.getResCd()) { //요청금액 > 잔액
            throw new RequestValidationException(ErrorCode.WITHDRAW_AMT_ERROR);

        }  else if (resCode == ProcResCd.LOW_LOCK_CUST_WALLET.getResCd()) {
            throw new RequestValidationException(ErrorCode.LOW_LOCK_CUST_WALLET);

        }else if (resCode == ProcResCd.RETRY_NEEDED.getResCd() || resCode == ProcResCd.ERROR.getResCd()) {
            log.info("거래실패: {}", resMsg);
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE);
        }else{
            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE, " [관리자 출금 오류]");
        }
    }
}
