package kr.co.hectofinancial.mps.api.v1.common.service;

import kr.co.hectofinancial.mps.api.v1.card.domain.BpcMOpenInfo;
import kr.co.hectofinancial.mps.api.v1.card.repository.BpcMOpenInfoRepository;
import kr.co.hectofinancial.mps.api.v1.common.domain.Holiday;
import kr.co.hectofinancial.mps.api.v1.common.dto.GetCustChrgMeanResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.repository.HolidayRepository;
import kr.co.hectofinancial.mps.api.v1.cpn.service.CpnService;
import kr.co.hectofinancial.mps.api.v1.customer.domain.CustChrgMean;
import kr.co.hectofinancial.mps.api.v1.customer.domain.Customer;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerRequestDto;
import kr.co.hectofinancial.mps.api.v1.customer.repository.CustChrgMeanRepository;
import kr.co.hectofinancial.mps.api.v1.customer.repository.CustomerRepository;
import kr.co.hectofinancial.mps.api.v1.market.domain.*;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MpsMarketDto;
import kr.co.hectofinancial.mps.api.v1.market.repository.*;
import kr.co.hectofinancial.mps.api.v1.notification.repository.SitePolicyMastRepository;
import kr.co.hectofinancial.mps.api.v1.trade.domain.TrdDivDtl;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TrdDivDtlRepository;
import kr.co.hectofinancial.mps.global.constant.*;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonService {
    /**
     * db 를 접근하는 공통 서비스
     */
    private final MarketOpenInfoRepository marketOpenInfoRepository;
    private final MarketAddInfoRepository marketAddInfoRepository;
    private final CustomerRepository customerRepository;
    private final CustChrgMeanRepository custChrgMeanRepository;
    private final HolidayRepository holidayRepository;
    private final MpsMarketChrgMapRepository mpsMarketChrgMapRepository;
    private final MarketServiceProductRepository marketServiceProductRepository;
    private final TrdDivDtlRepository trdDivDtlRepository;
    private final CpnService cpnService;
    private final SitePolicyMastRepository sitePolicyMastRepository;
    private final MpsMarketRepository mpsMarketRepository;
    private final BpcMOpenInfoRepository bpcMOpenInfoRepository;

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByCustNo(String custNo) {

        if(CommonUtil.nullTrim(custNo).equals("")){
            throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        Customer customer = customerRepository.findCustomerByMpsCustNo(custNo)
                .orElseThrow(() -> new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND));
        return CustomerDto.of(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByCustNoAndMId(String custNo, String mId) {

        if (CommonUtil.nullTrim(custNo).equals("")) {
            throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        if (CommonUtil.nullTrim(mId).equals("")) {
            throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        Customer customer = customerRepository.findCustomerByMpsCustNoAndMid(custNo, mId)
                .orElseThrow(() -> new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND));
        return CustomerDto.of(customer);
    }
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByMCustId(String mCustIdEnc, MarketAddInfoDto marketAddInfo) {
        String mCustId = "";
        String mId = marketAddInfo.getMid();
        try {
            mCustId = CipherUtil.decrypt(mCustIdEnc, marketAddInfo.getEncKey());
        } catch (Exception e) {
            throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND_BY_CUST_ID);
        }
        if(CommonUtil.nullTrim(mCustId).equals("")){
            throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND_BY_CUST_ID);
        }
        Customer customer = customerRepository.findCustomerByMCustIdAndMid(mCustId, mId)
                .orElseThrow(() -> new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND_BY_CUST_ID));
        return CustomerDto.of(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getValidCustomerForCustomerInfo(CustomerRequestDto customerRequestDto) throws Exception {
        CustomerDto customerDto = customerRequestDto.getCustomerDto();

        if (Objects.isNull(customerDto)) {
            throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        //회원ci 체크
        String requestParamCi = customerRequestDto.getCi();
        if (!CustStatCd.WITHDRAW.getStatCd().equals(customerDto.getStatCd())) {
            //상태값 "해지" 아니면 ci 값 비교
            if (BizDivCd.CORPORATE.getBizDivCd().equals(customerDto.getBizDivCd())|| BizDivCd.PERSONAL.getBizDivCd().equals(customerDto.getBizDivCd())) {
                //법인 || 개인 (사업자)
                if (!customerDto.getBizRegNo().equals(requestParamCi)) {
                    log.info("***** 사업자 CI값(사업자등록번호) 불일치=> DB [{}] 요청값 [{}]", customerDto.getBizRegNo(), requestParamCi);
                    throw new RequestValidationException(ErrorCode.BIZ_REG_NO_NOT_MATCH);
                }
            }else{
                //개인
                if (!customerDto.getCiEnc().equals(requestParamCi)) {
                    log.info("*****CI값 불일치=> DB CI [{}] 요청값 [{}]", customerDto.getCiEnc(), requestParamCi);
                    throw new RequestValidationException(ErrorCode.CI_NOT_MATCH);
                }
            }
        }
        //회원아이디 체크
        String requestCustNo = customerRequestDto.getCustNo();
        String requestParamCustId = customerRequestDto.getCustId();
        if (StringUtils.isNotBlank(requestCustNo) && StringUtils.isNotBlank(requestParamCustId)) {
            if (!customerDto.getMCustId().equals(requestParamCustId)) {
                log.info(">>>회원정보조회 mCustId 불일치 조회:[{}], 입력:[{}]<<<", customerDto.getMCustId(), requestParamCustId);
                throw new RequestValidationException(ErrorCode.WRONG_CUST_ID);
            }
        }
        return customerDto;
    }

    @Transactional(readOnly = true)
    public MarketAddInfoDto getMarketAddInfoByMId(String mid) {
        MarketAddInfo marketAddInfo = marketAddInfoRepository.findMarketAddInfoByMidAndSysdateBetween(mid)
                .orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));
        return MarketAddInfoDto.of(marketAddInfo);

    }

    @Transactional(readOnly = true)
    public Optional<MarketAddInfo> getOnlyMarketAddInfoByMId(String mid) {
        return marketAddInfoRepository.findMarketAddInfoByMidAndSysdateBetween(mid);
    }

    @Transactional(readOnly = true)
    public MarketAddInfoDto getMarketAddInfoByCustNo(String custNo) {
        return getMarketAddInfoByMId(getCustomerByCustNo(custNo).getMid());
    }

    @Transactional(readOnly = true)
    public void validateMarketOpenInfoByMIdAndPrdtCd(String mid, String svcCd, String prdtCd) {

        if(CommonUtil.nullTrim(svcCd).equals("")){
            svcCd = MpsApiCd.SVC_CD;
        }

        /* 가맹점 상태 검증 */
        MarketOpenInfo tgtMarketOpenInfo = marketOpenInfoRepository.findMarketOpenInfoByMidAndSvcCdAndPrdtCdAndOpenRangeCdAndStDateBetween(mid, svcCd, prdtCd, "A", "Y").orElse(null);

        if (tgtMarketOpenInfo == null) {
            throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
        } else {
            //open_stat_cd = Y (정상오픈)
            if (tgtMarketOpenInfo.getOpenStatCd().equals("N") || tgtMarketOpenInfo.getOpenStatCd().equals("T")) {
                throw new RequestValidationException(ErrorCode.MARKET_STATUS_NOT_VALID);
            }
        }
    }

    @Transactional(readOnly = true)
    public void validateMarketOpenInfoByMIdAndPrdtCdTemp(String mid, String prdtCd) {
        MarketOpenInfo tgtMarketOpenInfo = marketOpenInfoRepository.findMarketOpenInfoByMidAndSvcCdAndPrdtCdAndOpenRangeCdAndStDateBetween(mid, MpsApiCd.SVC_CD, prdtCd, "A").orElse(null);
        if (tgtMarketOpenInfo == null) {
            throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
        }
    }

    /* 회원 충전수단 GET */
    @Transactional(readOnly = true)
    public GetCustChrgMeanResponseDto getCustChrgMean(String custNo, String chrgMeanCd) {
        GetCustChrgMeanResponseDto custChrgMeanResponseDto = null;

        CustChrgMean tgtCustChrgMean = custChrgMeanRepository.findByMpsCustNoAndChrgMeanCd(custNo, chrgMeanCd)
                .orElseThrow(() -> new RequestValidationException(ErrorCode.CUSTOMER_CHRG_MEAN_NOT_FOUND));

        String tgtJson = tgtCustChrgMean.getChrgMeanDtl();
        if(tgtJson == null){
            throw new RequestValidationException(ErrorCode.CUSTOMER_CHRG_MEAN_NOT_FOUND);
        }else{
            JSONObject jsonObject = new JSONObject(tgtJson);
            String decBankCd = "";
            String decAccountNo = "";
            String decCustNm = "";
            String decBirthDt = "";
            String decBizRegNo = "";
            String decWithdrawYn = "";
            String birthDt = null;
            String bizRegNo = null;
            String bankCd = jsonObject.getString("bankCd");
            String accountNo = jsonObject.getString("accountNo");
            String custNm = jsonObject.getString("custNm");
            String withdrawYn = jsonObject.getString("withdrawYn");

            if(jsonObject.has("birthDt")){
                birthDt = jsonObject.getString("birthDt");
            }else if(jsonObject.has("bizRegNo")){
                bizRegNo = jsonObject.getString("bizRegNo");
            }

            /* 복호화 */
            try {
                DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
                decBankCd = databaseAESCryptoUtil.convertToEntityAttribute(bankCd);
                decAccountNo = databaseAESCryptoUtil.convertToEntityAttribute(accountNo);
                decCustNm = databaseAESCryptoUtil.convertToEntityAttribute(custNm);
                decWithdrawYn = databaseAESCryptoUtil.convertToEntityAttribute(withdrawYn);

                if(birthDt != null){
                    decBirthDt = databaseAESCryptoUtil.convertToEntityAttribute(birthDt);
                }else if(bizRegNo != null){
                    decBizRegNo = databaseAESCryptoUtil.convertToEntityAttribute(bizRegNo);
                }
//            log.info("bankCd: [{}], accountNo: [{}], custNm: [{}], birthDt: [{}], withdrawYn: [{}], bizRegNo: [{}]", decBankCd, decAccountNo, decCustNm, decBirthDt, withdrawYn, bizRegNo);
            } catch (Exception e) {
                throw new RequestValidationException(ErrorCode.DECRYPT_ERROR);
            }

            /* 출금(환불)계좌등록 동의 여부 검증 */
            if (decWithdrawYn.equals("N")) {
                throw new RequestValidationException(ErrorCode.CUSTOMER_CHRG_MEAN_NOT_FOUND);
            }

            custChrgMeanResponseDto = GetCustChrgMeanResponseDto.builder()
                    .custNo(custNo)
                    .chrgMeanCd(chrgMeanCd)
                    .bankCd(decBankCd)
                    .accountNo(decAccountNo)
                    .aesAccountNo(accountNo)
                    .custNm(decCustNm)
                    .birthDt(decBirthDt)
                    .bizRegNo(decBizRegNo)
                    .build();

            log.info("회원 충전수단 >>> custNo={} chrgMeanCd={} bankCd={} accountNo={} custNm={} birthDt={} bizRegNo={}",
                    custNo, chrgMeanCd, bankCd, accountNo, custNm, birthDt, bizRegNo);
        }

        return custChrgMeanResponseDto;
    }

    /*가맹점 상태확인 */ // --> 회원가입에서만 검증
//    @Transactional(readOnly = true)
//    public void checkMarketOpenByMId(String mId) {
//        MpsMarket mpsMarket = mpsMarketRepository.findMpsMarketByMid(mId).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));
//        if ("N".equals(mpsMarket.getUseYn().toUpperCase())) {
//            throw new RequestValidationException(ErrorCode.MARKET_STATUS_NOT_VALID);
//        }
//    }

    /**
     * 영업일 계산
     *
     * @param curDt
     * @param offSet
     * @return
     */
    @Transactional(readOnly = true)
    public String workingDay(String curDt, int offSet) {

        int checkCnt = 0;
        String targetDt = curDt;

        if (offSet < 0) {
            throw new IllegalArgumentException("offSet must be >= 0");
        }

        while (checkCnt < offSet) {
            targetDt = DateTimeUtil.addDate(targetDt, 1);
            if (checkHoliday(targetDt)) {
                checkCnt++;
            }
        }
        return targetDt;
    }

    /**
     * @param tgtDt
     * @return 공휴일 : false, 영업일 : true
     */
    public boolean checkHoliday(String tgtDt) {

        Optional<Holiday> opt = holidayRepository.findByYmdAndLnrMonDivCd(tgtDt, "0");

        // 데이터 없으면 영업일
        if (!opt.isPresent()) {
            return true;
        }

        Holiday h = opt.get();
        return h.getHldStatCd().equals(HolidayStatCd.WORKING.getHldStatCd())
                && !h.getDayCd().equals("1")
                && !h.getDayCd().equals("7");
    }

    @Transactional(readOnly = true)
    public void checkMinChrgAmt(String mid, String chrgMeanCd, String trdDivCd, long chrgAmt) {

        log.info("잔액구분코드: [{}]", trdDivCd.substring(0, 1));
        MpsMarketChrgMap tgtMpsMarketChrgMap = mpsMarketChrgMapRepository.findAllByMidAndChrgMeanCd(mid, chrgMeanCd, trdDivCd.substring(0, 1)).orElseThrow(() ->
                new RequestValidationException(ErrorCode.MARKET_CHRG_CD_NOT_FOUND));

        long chrgUnitCd = Long.valueOf(tgtMpsMarketChrgMap.getMinChrgUnitCd());
        if (chrgAmt == 0) {
            throw new RequestValidationException(ErrorCode.TRADE_AMT_ERROR);
        }
        if (chrgAmt < tgtMpsMarketChrgMap.getMinChrgAmt()) {
            throw new RequestValidationException(ErrorCode.TRADE_MNM_AMT_ERROR);
        }
        if (chrgUnitCd != 0 && chrgAmt % chrgUnitCd != 0) {
            throw new RequestValidationException(ErrorCode.TRADE_CHRG_UNIT_CD_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public void checkValidStlMid(String stlMid, String mid) {

        String svcCd = MpsApiCd.SVC_CD;
        String prdtCd = MpsPrdtCd.use.getPrdtCd();
        Optional<MarketServiceProduct> market = marketServiceProductRepository.findMarketServiceProductByMidAndSvcCdAndPrdtCd(stlMid, svcCd, prdtCd);
        if (!market.isPresent()) {
            log.error("*** 유효하지 않은 정산 상점아이디 [{}] BAS.TB_M_SVC_PRDT 확인 필요 ", stlMid);
            throw new RequestValidationException(ErrorCode.INVALID_STL_MID);
        }

        // STL_M_ID != M_ID 면 CPN_ID 검증 => CPN_ID 같으면 통과
        if(!stlMid.equals(mid)){

            String mCpnId = cpnService.getCpnId(mid);
            log.info("*** BAS.TB_CPN_M 검증 M_ID: [{}], CPN_ID: [{}]", mid, mCpnId);

            String sCpnId = cpnService.getCpnId(stlMid);
            log.info("*** BAS.TB_CPN_M 검증 STL_MID: [{}], CPN_ID: [{}]", stlMid, sCpnId);

            if(!mCpnId.equals(sCpnId)){ //CPN_ID 같으면 통과

                if(!stlMid.equals(marketServiceProductRepository.validStlMid(stlMid, mid))){
                    log.error("*** 정산 상점아이디 오류 BAS.TB_CPN_M 검증 오류 ***");
                    throw new RequestValidationException(ErrorCode.INVALID_STL_MID);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public void checkValidStlMid(String stlMid, String mid, String prdtCd) {

        String svcCd = MpsApiCd.SVC_CD;
        Optional<MarketServiceProduct> market = marketServiceProductRepository.findMarketServiceProductByMidAndSvcCdAndPrdtCd(stlMid, svcCd, prdtCd);
        if (!market.isPresent()) {
            log.error("*** 유효하지 않은 정산 상점아이디 [{}] BAS.TB_M_SVC_PRDT 확인 필요 ", stlMid);
            throw new RequestValidationException(ErrorCode.INVALID_STL_MID);
        }

        // STL_M_ID != M_ID 면 CPN_ID 검증 => CPN_ID 같으면 통과
        if(!stlMid.equals(mid)){

            String mCpnId = cpnService.getCpnId(mid);
            log.info("*** BAS.TB_CPN_M 검증 M_ID: [{}], CPN_ID: [{}]", mid, mCpnId);

            String sCpnId = cpnService.getCpnId(stlMid);
            log.info("*** BAS.TB_CPN_M 검증 STL_MID: [{}], CPN_ID: [{}]", stlMid, sCpnId);

            if(!mCpnId.equals(sCpnId)){ //CPN_ID 같으면 통과

                if(!stlMid.equals(marketServiceProductRepository.validStlMid(stlMid, mid, prdtCd))){
                    log.error("*** 정산 상점아이디 오류 BAS.TB_CPN_M 검증 오류 ***");
                    throw new RequestValidationException(ErrorCode.INVALID_STL_MID);
                }
            }
        }
    }

    /* 충전취소 가능 여부 검증(충전수단) */
    public String checkChrgCnclPsblYn(String mid, String chrgMeanCd, String orgDt){

        String result;
        MpsMarketChrgMap mpsMarketChrgMap = mpsMarketChrgMapRepository.findByChrgCnclPsblYn(mid, chrgMeanCd, orgDt);
        if(mpsMarketChrgMap == null){
            throw new RequestValidationException(ErrorCode.MARKET_CHRG_CD_NOT_FOUND);
        }else{
            if(CommonUtil.nullTrim(mpsMarketChrgMap.getChrgCnclPlcCd()).equals("")){
                //todo BO 개발이 될때까지는 해당 로직 살려둠
                result = "A";
                if(mpsMarketChrgMap.getChrgCnclPsblYn().equals("N")){
                    throw new RequestValidationException(ErrorCode.NOT_POSSIBLE_CHARGE_CANCEL);
                }
            }else {
                result = mpsMarketChrgMap.getChrgCnclPlcCd();
                if(mpsMarketChrgMap.getChrgCnclPlcCd().equals("N")){
                    throw new RequestValidationException(ErrorCode.NOT_POSSIBLE_CHARGE_CANCEL);
                }
            }
        }
        //충전취소 정책코드 리턴
//        return mpsMarketChrgMap.getChrgCnclPlcCd();
        return result;
    }

    /* 거래상세구분코드 검증 */
    public void checkValidTrdDivDtlCd(String mid, String trdDivCd, String trdDivDtlCd){
        TrdDivDtl tgtTrdDivDtl = trdDivDtlRepository.findByMidAndTrdDivCdAndTrdDivDtlCdAndUseYn(mid, trdDivCd, trdDivDtlCd, "Y");
        if(tgtTrdDivDtl == null){
            throw new RequestValidationException(ErrorCode.TRADE_DIV_DTL_CD_ERROR);
        }
    }

    /* MPS_M 테이블 조회*/
    public MpsMarketDto getMpsMarketInfo(String mid) {
        MpsMarket mpsMarket = mpsMarketRepository.findMpsMarketByMid(mid).orElseThrow(() -> new RequestValidationException(ErrorCode.MPS_MARKET_NOT_FOUND));
        return MpsMarketDto.of(mpsMarket);
    }

    public boolean isMpsMarketBillKeyEnable(String mid) {
        MpsMarketDto mpsMarketInfo = getMpsMarketInfo(mid);
        return "Y".equals(mpsMarketInfo.getBillKeyUseYn());
    }

    public void checkValidMidForCardUse(String trdDivCd, String mid, String ornId){

        log.info(">>>> 카드사용 가맹점 검증 TRD_DIV_CD: [{}], M_ID: [{}], ORN_ID: [{}]", trdDivCd, mid, ornId);
        BpcMOpenInfo tgtBpcMOpenInfo = bpcMOpenInfoRepository.findByMidAndOrnIdAndStDateAndEdDate(mid, ornId, "Y");
        if (tgtBpcMOpenInfo == null) {
            throw new RequestValidationException(ErrorCode.MARKET_STATUS_NOT_VALID, "(카드사용 대상 가맹점이 아닙니다.)");
        }

        if(TrdDivCd.COMMON_USE.getTrdDivCd().equals(trdDivCd)){
            if(!tgtBpcMOpenInfo.getOpenRangeCd().equals("A") && !tgtBpcMOpenInfo.getOpenRangeCd().equals(TrdDivCd.COMMON_USE.getTrdDivCd())){
                throw new RequestValidationException(ErrorCode.MARKET_STATUS_NOT_VALID, "(카드사용/사용취소 불가능 가맹점)");
            }
        }else if(TrdDivCd.USE_COMMON_CANCEL.getTrdDivCd().equals(trdDivCd)){
            if(!tgtBpcMOpenInfo.getOpenRangeCd().equals("A") && !tgtBpcMOpenInfo.getOpenRangeCd().equals(TrdDivCd.USE_COMMON_CANCEL.getTrdDivCd())){
                throw new RequestValidationException(ErrorCode.MARKET_STATUS_NOT_VALID, "(카드사용/사용취소 불가능 가맹점)");
            }
        }else{
            throw new RequestValidationException(ErrorCode.MARKET_STATUS_NOT_VALID, "(카드사용 대상 가맹점이 아닙니다.)");
        }
    }


}

