package kr.co.hectofinancial.mps.api.v1.trade.service.admin;

import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.customer.repository.CustomerRepository;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.trade.domain.AdminTrdDtl;
import kr.co.hectofinancial.mps.api.v1.trade.domain.AdminTrdReq;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminWithdrawApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.PointRevokeResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.repository.AdminTrdDtlRepository;
import kr.co.hectofinancial.mps.api.v1.trade.repository.AdminTrdReqRepository;
import kr.co.hectofinancial.mps.api.v1.trade.service.wallet.WalletService;
import kr.co.hectofinancial.mps.global.constant.*;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static kr.co.hectofinancial.mps.global.util.CipherSha256Util.digestSHA256;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncService {

    @Value("${spring.profiles.active}")
    private String profiles;
    private final AdminTrdReqRepository adminTrdReqRepository;
    private final AdminTrdDtlRepository adminTrdDtlRepository;
    private final CustomerRepository customerRepository;
    private final WalletService walletService;
    private final CommonService commonService;
    private final IsolatedService isolatedService;
    private final AdminResultSaver adminResultSaver;

    @Async("async")
    @Transactional(rollbackFor = Exception.class)
    public void adminTrade2(AdminChargeApprovalRequestDto adminChargeApprovalRequestDto){

        int totCnt = 0;
        int sucCnt = 0;
        int failCnt = 0;
        long totAmt = 0;
        long sucAmt = 0;
        long failAmt = 0;
        String divCd = "";

        List<String> procStatCdList = new ArrayList<>(Arrays.asList(AdminProcStatCd.A.getProcStatCd(), AdminProcStatCd.WAITTING.getProcStatCd()));
        AdminTrdReq tgtAdminTrdReq = adminTrdReqRepository.findByTrdReqNoAndProcStatCdInAndMid(adminChargeApprovalRequestDto.getTrdReqNo(), procStatCdList, adminChargeApprovalRequestDto.getMid());
        if(tgtAdminTrdReq == null){
            throw new RequestValidationException(ErrorCode.ADMIN_CHARGE_ERROR, "올바르지 않은 TRD_REQ_NO 입니다.");
        }

        tgtAdminTrdReq.saveProcStDate();

        divCd = tgtAdminTrdReq.getTrdDivCd();
        log.info(">>>> 관리자 수기 거래 리스트 중복체크 async-start [{}], TRD_REQ_NO: [{}] <<<<", divCd, tgtAdminTrdReq.getTrdReqNo());

        List<AdminTrdDtl> tgtAdminTrdDtlList = adminTrdDtlRepository.findByTrdReqNoAndRsltStatCd(adminChargeApprovalRequestDto.getTrdReqNo(), AdminRsltStatCd.WAITTING.getRsltStatCd());

        if(tgtAdminTrdDtlList.size() == 0){
            throw new RequestValidationException(ErrorCode.ADMIN_CHARGE_ERROR, "올바르지 않은 TRD_REQ_NO 입니다.");
        }
        /* 금액, 건수 SET */
        totCnt = tgtAdminTrdDtlList.size();
        totAmt = tgtAdminTrdReq.getReqAmt();

        Set<Integer> chkDuplicate= tgtAdminTrdDtlList.stream()
                .collect(Collectors.groupingBy(
                        e -> Objects.hash(e.getCustNmEnc(), e.getCphoneNoEnc()), //복합키 해시값 생성
                        Collectors.counting() // 각 해시값에 대한 개수
                ))
                .entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        boolean chkAdminTrdList = !chkDuplicate.isEmpty(); // 비어있으면 중복x
        log.info(">>> 관리자 수기 거래 리스트 중복 여부 TRD_REQ_NO: [{}]<<<: ", tgtAdminTrdReq.getTrdReqNo(), chkAdminTrdList);

        if (chkAdminTrdList) {
            failCnt = totCnt;
            failAmt = totAmt;
            tgtAdminTrdDtlList.stream()
                    .forEach(e -> {
                        tgtAdminTrdReq.saveResult(AdminProcStatCd.FAIL.getProcStatCd(), 0, 0, tgtAdminTrdReq.getReqCnt(), tgtAdminTrdReq.getReqAmt());
                    });

            tgtAdminTrdReq.saveResult(AdminProcStatCd.FAIL.getProcStatCd(), 0, 0, tgtAdminTrdReq.getReqCnt(), tgtAdminTrdReq.getReqAmt());
            log.info(">>>> 머니/포인트 관리자 수기 거래 => 총건수: [{}], 총금액: [{}], 성공건수: [{}], 성공금액: [{}], 실패건수: [{}], 실패금액: [{}] <<<<", totCnt, totAmt, sucCnt, sucAmt, failCnt, failAmt);
            return;
        }

        //회원정보조회
        for (AdminTrdDtl list : tgtAdminTrdDtlList) {
            long bfBlc = 0;

            log.info(">>>> 관리자 수기 머니/포인트 거래 async-start [{}], TRD_REQ_NO: [{}], SEQ_NO: [{}] <<<<", divCd, tgtAdminTrdReq.getTrdReqNo(), list.getSeqNo());

            List<String> custNoList = customerRepository.findByCustNmAndCphoneNoEncAndMidAndBizDivCd(list.getCustNmEnc(), list.getCphoneNoEnc(), tgtAdminTrdReq.getMid(), BizDivCd.INDIVIDUAL.getBizDivCd());
            log.info("대상회원 CUST_NO :[{}]", custNoList);

            if (custNoList.size() != 1 || custNoList.size() == 0) {//여러명이거나 한명도 없으면
                log.error(">>>> 회원조회 오류 SEQ_NO: [{}] <<<<", list.getSeqNo());
                adminResultSaver.saveValue(AdminFailureType.HAS_NO_CUST.getFailureType(), list, null, 0);
                log.info("---- 회원 조회 오류 종료------");
                failCnt++;
                failAmt += list.getTrdAmt();
                continue;
            }

            adminResultSaver.saveValue(AdminFailureType.HAS_CUST.getFailureType(), list, custNoList.get(0), 0);

            /* 잔액조회 */
            try {
                bfBlc = isolatedService.getCustWallet(list.getRmk(), divCd);
                adminResultSaver.saveValue(AdminFailureType.BF_BLC.getFailureType(), list, list.getRmk(), bfBlc);

            } catch (Exception e) {
                log.error(">>>> 잔액조회 오류 SEQ_NO: [{}], 회원번호: [{}] <<<<", list.getSeqNo(), list.getRmk());
                adminResultSaver.saveFailureOrSuccess(AdminFailureType.FAIL.getFailureType(), list, ErrorCode.GET_BALANCE_ERROR.getErrorCode(), ErrorCode.GET_BALANCE_ERROR.getErrorMessage());
                failCnt++;
                failAmt += list.getTrdAmt();
            }
            //TODO rmk not null 인 회원 여기서 지급하면 안되는지
        }

        List<AdminTrdDtl> filteredList = adminTrdDtlRepository.findByTrdReqNoAndRsltStatCdAndRmkIsNotNull(adminChargeApprovalRequestDto.getTrdReqNo(), AdminRsltStatCd.WAITTING.getRsltStatCd());

        for (AdminTrdDtl list : filteredList) {
            CustomerDto customerDto = null;
            long afBlc = 0;

            try {
                //회원정보 추출
                customerDto = commonService.getCustomerByCustNoAndMId(list.getRmk(), tgtAdminTrdReq.getMid());

            } catch (Exception e) {
                log.error(">>>> 회원조회 오류 SEQ_NO: [{}] <<<<", list.getSeqNo());
                adminResultSaver.saveValue(AdminFailureType.HAS_NO_CUST.getFailureType(), list, null, 0);
                failCnt++;
                failAmt += list.getTrdAmt();
                continue;
            }

            try {
                log.info(">>>> 관리자 수기 머니/포인트 충전 async-start [{}], TRD_REQ_NO: [{}], SEQ_NO: [{}] <<<<", divCd, tgtAdminTrdReq.getTrdReqNo(), list.getSeqNo());
                MarketAddInfoDto marketAddInfo = commonService.getMarketAddInfoByMId(customerDto.getMid());
                String pktHashStr = list.getRmk() + customerDto.getMid() + list.getSeqNo() + list.getTrdAmt();
                String pktHash = digestSHA256(pktHashStr + marketAddInfo.getPktHashKey());
                ChargeApprovalResponseDto chargeApprovalResponseDto = (ChargeApprovalResponseDto) isolatedService.adminTradeRequest(tgtAdminTrdReq, list, customerDto, pktHash);

                if(chargeApprovalResponseDto != null){
                    /* 성공횟수 금액 추가 거래 후 잔액 결과값 추가 */
                    if(TrdDivCd.MONEY_PROVIDE.getTrdDivCd().equals(divCd)){
                        afBlc = Long.parseLong(chargeApprovalResponseDto.getMnyBlc());
                    }else if(TrdDivCd.POINT_PROVIDE.getTrdDivCd().equals(divCd)){
                        afBlc = Long.parseLong(chargeApprovalResponseDto.getPntBlc());
                    }

                    list.saveResultCode(AdminRsltStatCd.SUCCESS.getRsltStatCd(), "0000", null);
                    list.saveTrdNo(chargeApprovalResponseDto.getTrdNo(), chargeApprovalResponseDto.getTrdDt(), chargeApprovalResponseDto.getTrdTm());
                    list.saveAfBlc(afBlc);
                    sucCnt++;
                    sucAmt += list.getTrdAmt();
                }
            } catch (Exception e){
                log.error(">>>> 충전 오류 SEQ_NO: [{}], 회원번호: [{}] <<<<", list.getSeqNo(), list.getRmk());
                list.saveResultCode(AdminRsltStatCd.FAIL.getRsltStatCd(), ErrorCode.fromErrorMessage(e.getMessage()).getErrorCode(), e.getMessage());
                list.saveAfBlc(afBlc);
                failCnt++;
                failAmt += list.getTrdAmt();
                e.printStackTrace();
            }
        }

        String procStatCd = AdminProcStatCd.FAIL.getProcStatCd();
        if(sucAmt > 0 && sucCnt > 0 && failCnt > 0){
            procStatCd = AdminProcStatCd.P.getProcStatCd();
        }else if(sucAmt > 0 && sucCnt > 0 && failCnt == 0){
            procStatCd = AdminProcStatCd.SUCCESS.getProcStatCd();
        }
        tgtAdminTrdReq.saveResult(procStatCd, sucCnt, sucAmt, failCnt, failAmt);
        log.info(">>>> 머니/포인트 관리자 수기 지급 => 총건수: [{}], 총금액: [{}], 성공건수: [{}], 성공금액: [{}], 실패건수: [{}], 실패금액: [{}] <<<<", totCnt, totAmt, sucCnt, sucAmt, failCnt, failAmt);
    }

    @Async("async")
    @Transactional(rollbackFor = Exception.class)
    public void adminTrade(AdminChargeApprovalRequestDto adminChargeApprovalRequestDto){

        int totCnt = 0;
        int sucCnt = 0;
        int failCnt = 0;
        long totAmt = 0;
        long sucAmt = 0;
        long failAmt = 0;
        String divCd = "";

        if(!profiles.equals("test") && !profiles.equals("dev") && !profiles.equals("local")){
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                log.error(">>>>관리자 수기 거래  Thread sleep 오류 [{}] TRD_REQ_NO:[{}]<<<<", e.getMessage(), adminChargeApprovalRequestDto.getTrdReqNo());
            }
        }

        List<String> procStatCdList = new ArrayList<>(Arrays.asList(AdminProcStatCd.A.getProcStatCd(), AdminProcStatCd.WAITTING.getProcStatCd()));
        AdminTrdReq tgtAdminTrdReq = adminTrdReqRepository.findByTrdReqNoAndProcStatCdInAndMid(adminChargeApprovalRequestDto.getTrdReqNo(), procStatCdList, adminChargeApprovalRequestDto.getMid());
        if(tgtAdminTrdReq == null){
            throw new RequestValidationException(ErrorCode.ADMIN_CHARGE_ERROR, "올바르지 않은 TRD_REQ_NO 입니다.");
        }

        tgtAdminTrdReq.saveProcStDate();

        divCd = tgtAdminTrdReq.getTrdDivCd();
        log.info(">>>>관리자 수기 거래 리스트 중복체크 async-start [{}], TRD_REQ_NO: [{}]<<<<", divCd, tgtAdminTrdReq.getTrdReqNo());

        List<AdminTrdDtl> tgtAdminTrdDtlList = adminTrdDtlRepository.findByTrdReqNoAndRsltStatCd(adminChargeApprovalRequestDto.getTrdReqNo(), AdminRsltStatCd.WAITTING.getRsltStatCd());

        if(tgtAdminTrdDtlList.size() == 0){
            throw new RequestValidationException(ErrorCode.ADMIN_CHARGE_ERROR, "올바르지 않은 TRD_REQ_NO 입니다.");
        }
        /* 금액, 건수 SET */
        totCnt = tgtAdminTrdDtlList.size();
        totAmt = tgtAdminTrdReq.getReqAmt();

        Set<Integer> chkDuplicate= tgtAdminTrdDtlList.stream()
                .collect(Collectors.groupingBy(
                        e -> Objects.hash(e.getCustNmEnc(), e.getCphoneNoEnc()), //복합키 해시값 생성
                        Collectors.counting() // 각 해시값에 대한 개수
                ))
                .entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        boolean chkAdminTrdList = !chkDuplicate.isEmpty(); // 비어있으면 중복x
        log.info(">>>관리자 수기 거래 리스트 중복 여부 TRD_REQ_NO: [{}], [{}]<<<: ", tgtAdminTrdReq.getTrdReqNo(), chkAdminTrdList);

        if (chkAdminTrdList) {
            failCnt = totCnt;
            failAmt = totAmt;
            tgtAdminTrdDtlList.stream()
                    .forEach(e -> {
                        tgtAdminTrdReq.saveResult(AdminProcStatCd.FAIL.getProcStatCd(), 0, 0, tgtAdminTrdReq.getReqCnt(), tgtAdminTrdReq.getReqAmt());
                    });

            tgtAdminTrdReq.saveResult(AdminProcStatCd.FAIL.getProcStatCd(), 0, 0, tgtAdminTrdReq.getReqCnt(), tgtAdminTrdReq.getReqAmt());
            log.info(">>>>머니/포인트 관리자 수기 거래 => 총건수: [{}], 총금액: [{}], 성공건수: [{}], 성공금액: [{}], 실패건수: [{}], 실패금액: [{}]<<<<", totCnt, totAmt, sucCnt, sucAmt, failCnt, failAmt);
            return;
        }

        List<AdminTrdDtl> adminTrdDtlArrayList = new ArrayList<>();
        for(AdminTrdDtl list : tgtAdminTrdDtlList){

            AdminTrdDtl copyAdminTrdDtl = new AdminTrdDtl();
            BeanUtils.copyProperties(list, copyAdminTrdDtl);

            /* 회원조회 */
            long bfBlc = 0;

            log.info(">>>>관리자 수기 머니/포인트 회원조회<<<< [{}], TRD_REQ_NO: [{}], SEQ_NO: [{}]", divCd, tgtAdminTrdReq.getTrdReqNo(), list.getSeqNo());

            List<String> custNoList;
            if(divCd.equals(TrdDivCd.MONEY_PROVIDE.getTrdDivCd()) || divCd.equals(TrdDivCd.POINT_PROVIDE.getTrdDivCd())){
                custNoList = customerRepository.findByCustNmAndCphoneNoEncAndMidAndBizDivCd(list.getCustNmEnc(), list.getCphoneNoEnc(), tgtAdminTrdReq.getMid(), BizDivCd.INDIVIDUAL.getBizDivCd());
            }else{
                custNoList = customerRepository.findByCustNmAndCphoneNoEncAndMidAndStatCdNotInAndBizDivCd(list.getCustNmEnc(), list.getCphoneNoEnc(), tgtAdminTrdReq.getMid(), BizDivCd.INDIVIDUAL.getBizDivCd());
            }

            log.info("대상회원 CUST_NO :[{}]", custNoList);

            if (custNoList.size() != 1 || custNoList.size() == 0) {//여러명이거나 한명도 없으면
                log.error(">>>>회원조회 오류<<<< SEQ_NO: [{}]", list.getSeqNo());
                copyAdminTrdDtl.saveResultCode(AdminRsltStatCd.FAIL.getRsltStatCd(), ErrorCode.CUSTOMER_NOT_FOUND.getErrorCode(), ErrorCode.CUSTOMER_NOT_FOUND.getErrorMessage());
                adminTrdDtlArrayList.add(copyAdminTrdDtl);
                failCnt++;
                failAmt += list.getTrdAmt();
                continue;
            }

            copyAdminTrdDtl.setRmk(custNoList.get(0));
            adminTrdDtlArrayList.add(copyAdminTrdDtl);

            /* 잔액조회 */
            try {
                bfBlc = isolatedService.getCustWallet(copyAdminTrdDtl.getRmk(), divCd);
                copyAdminTrdDtl.setProcBfBlc(bfBlc);
                adminTrdDtlArrayList.add(copyAdminTrdDtl);
            } catch (Exception e) {
                log.error(">>>>잔액조회 오류<<<< SEQ_NO: [{}], 회원번호: [{}]", copyAdminTrdDtl.getSeqNo(), copyAdminTrdDtl.getRmk());
                copyAdminTrdDtl.saveResultCode(AdminRsltStatCd.FAIL.getRsltStatCd(), ErrorCode.GET_BALANCE_ERROR.getErrorCode(), ErrorCode.GET_BALANCE_ERROR.getErrorMessage());
                failCnt++;
                failAmt += list.getTrdAmt();
                adminTrdDtlArrayList.add(copyAdminTrdDtl);
                continue;
            }

            CustomerDto customerDto = null;
            long afBlc = 0;
            //회원정보 추출
            customerDto = commonService.getCustomerByCustNoAndMId(copyAdminTrdDtl.getRmk(), tgtAdminTrdReq.getMid());

            try {
                log.info(">>>>관리자 수기 머니/포인트 거래<<<< [{}], TRD_REQ_NO: [{}], SEQ_NO: [{}]", divCd, tgtAdminTrdReq.getTrdReqNo(), list.getSeqNo());
                MarketAddInfoDto marketAddInfo = commonService.getMarketAddInfoByMId(customerDto.getMid());
                String pktHashStr = copyAdminTrdDtl.getRmk() + customerDto.getMid() + list.getSeqNo() + list.getTrdAmt();
                String pktHash = digestSHA256(pktHashStr + marketAddInfo.getPktHashKey());

                String trdNo = "";
                String trdDt = "";
                String trdTm = "";

                if(divCd.equals(TrdDivCd.MONEY_PROVIDE.getTrdDivCd()) || divCd.equals(TrdDivCd.POINT_PROVIDE.getTrdDivCd())){
                    ChargeApprovalResponseDto chargeApprovalResponseDto = (ChargeApprovalResponseDto) isolatedService.adminTradeRequest(tgtAdminTrdReq, copyAdminTrdDtl, customerDto, pktHash);

                    if(TrdDivCd.MONEY_PROVIDE.getTrdDivCd().equals(divCd)){
                        afBlc = Long.parseLong(chargeApprovalResponseDto.getMnyBlc());
                    }else if(TrdDivCd.POINT_PROVIDE.getTrdDivCd().equals(divCd)){
                        afBlc = Long.parseLong(chargeApprovalResponseDto.getPntBlc());
                    }
                    trdNo = chargeApprovalResponseDto.getTrdNo();
                    trdDt = chargeApprovalResponseDto.getTrdDt();
                    trdTm = chargeApprovalResponseDto.getTrdTm();

                }else if(divCd.equals(TrdDivCd.MONEY_WITHDRAW.getTrdDivCd()) || divCd.equals(TrdDivCd.WAITMONEY_WITHDRAW.getTrdDivCd())){
                    AdminWithdrawApprovalResponseDto adminWithdrawApprovalResponseDto = (AdminWithdrawApprovalResponseDto) isolatedService.adminTradeRequest(tgtAdminTrdReq, copyAdminTrdDtl, customerDto, pktHash);
                    afBlc = Long.parseLong(adminWithdrawApprovalResponseDto.getBlcAmt());
                    trdNo = adminWithdrawApprovalResponseDto.getTrdNo();
                    trdDt = adminWithdrawApprovalResponseDto.getTrdDt();
                    trdTm = adminWithdrawApprovalResponseDto.getTrdTm();

                }else if(divCd.equals(TrdDivCd.POINT_REVOKE.getTrdDivCd())){
                    PointRevokeResponseDto pointRevokeResponseDto = (PointRevokeResponseDto) isolatedService.adminTradeRequest(tgtAdminTrdReq, copyAdminTrdDtl, customerDto, pktHash);
                    afBlc = Long.parseLong(pointRevokeResponseDto.getPntBlc());
                    trdNo = pointRevokeResponseDto.getTrdNo();
                    trdDt = pointRevokeResponseDto.getTrdDt();
                    trdTm = pointRevokeResponseDto.getTrdTm();
                }else{
                    log.error(">>>>TRD_DIV_CD 오류<<<< SEQ_NO: [{}], 회원번호: [{}]", copyAdminTrdDtl.getSeqNo(), copyAdminTrdDtl.getRmk());
                    copyAdminTrdDtl.saveResultCode(AdminRsltStatCd.FAIL.getRsltStatCd(), ErrorCode.TRADE_DIV_CD_ERROR.getErrorCode(), ErrorCode.TRADE_DIV_CD_ERROR.getErrorMessage());
                    failCnt++;
                    failAmt += list.getTrdAmt();
                    adminTrdDtlArrayList.add(copyAdminTrdDtl);
                    continue;
                }

                /* 성공횟수 결과값 추가 */
                copyAdminTrdDtl.saveResultCode(AdminRsltStatCd.SUCCESS.getRsltStatCd(), "0000", null);
                copyAdminTrdDtl.saveTrdNo(trdNo, trdDt, trdTm);
                copyAdminTrdDtl.saveAfBlc(afBlc);
                adminTrdDtlArrayList.add(copyAdminTrdDtl);
                sucCnt++;
                sucAmt += list.getTrdAmt();

            } catch (Exception e){
                log.error(">>>>충전/회수/출금 오류 DIV_CD: [{}], SEQ_NO: [{}], 회원번호: [{}]<<<<", divCd, copyAdminTrdDtl.getSeqNo(), copyAdminTrdDtl.getRmk());
                copyAdminTrdDtl.saveResultCode(AdminRsltStatCd.FAIL.getRsltStatCd(), ErrorCode.fromErrorMessage(e.getMessage()).getErrorCode(), e.getMessage());
                adminTrdDtlArrayList.add(copyAdminTrdDtl);
                failCnt++;
                failAmt += list.getTrdAmt();
                e.printStackTrace();
            }
        }
        adminTrdDtlRepository.saveAll(adminTrdDtlArrayList);

        String procStatCd = AdminProcStatCd.FAIL.getProcStatCd();
        if(sucAmt > 0 && sucCnt > 0 && failCnt > 0){
            procStatCd = AdminProcStatCd.P.getProcStatCd();
        }else if(sucAmt > 0 && sucCnt > 0 && failCnt == 0){
            procStatCd = AdminProcStatCd.SUCCESS.getProcStatCd();
        }else if(sucAmt == 0 && sucCnt == 0){
            procStatCd = AdminProcStatCd.FAIL.getProcStatCd();
        }
        tgtAdminTrdReq.saveResult(procStatCd, sucCnt, sucAmt, failCnt, failAmt);
        log.info(">>>>머니/포인트 관리자 수기 거래 => 총건수: [{}], 총금액: [{}], 성공건수: [{}], 성공금액: [{}], 실패건수: [{}], 실패금액: [{}]<<<<", totCnt, totAmt, sucCnt, sucAmt, failCnt, failAmt);

    }
}
