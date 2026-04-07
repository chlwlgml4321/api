package kr.co.hectofinancial.mps.api.v1.trade.service;

import kr.co.hectofinancial.mps.api.v1.customer.domain.Customer;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.customer.repository.CustomerRepository;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.CustomerWalletResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.ApprovalService;
import kr.co.hectofinancial.mps.api.v1.trade.service.wallet.WalletService;
import kr.co.hectofinancial.mps.global.constant.CustStatCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadTest {

    private final CustomerRepository customerRepository;
    private final WalletService walletService;
    private final ApprovalService approvalService;
    private final ApplicationContext applicationContext;

    @Transactional
    public void performLoadTest() throws InterruptedException {
        List<Customer> tgtCustomers = customerRepository.findTop2ByStatCdOrderByCreatedDateAsc(CustStatCd.STANDARD.getStatCd());
        int totCnt = tgtCustomers.size();
        CountDownLatch latch = new CountDownLatch(totCnt);

        for (Customer customer : tgtCustomers) {

            LoadTest self = applicationContext.getBean(LoadTest.class);
            self.performLoadTestForUser(customer, latch);
//            transactionTest2.performLoadTestForUser(customer, latch); //비동기 호출
        }

        //모든 작업이 완료될 때까지 대기
        // 최대 10초 동안 대기
        boolean completedInTime = latch.await(100, TimeUnit.SECONDS);
        if (completedInTime) {
            log.info("모든 작업이 완료되었습니다.");
        } else {
            log.info("시간 초과: 일부 작업이 완료되지 않았습니다.");
        }
    }

    @Async
    public void performLoadTestForUser(Customer customer, CountDownLatch latch) {
        Random random = new Random();

        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();

        try {
            log.info("사용자 " + customer.getMpsCustNo() + "의 작업 시작 - 스레드: " + Thread.currentThread().getName());
            // 1. 잔액조회
            String custNo = customer.getMpsCustNo();
            Customer tgtCustomer = customerRepository.findCustomerByMpsCustNo(custNo)
                    .orElseThrow(() -> new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND));
            CustomerDto tgtCustomerDto = CustomerDto.of(tgtCustomer);

            CustomerWalletResponseDto customerWalletDto = walletService.getCustWalletByCustNo(custNo);
            long currentMoney = customerWalletDto.getMnyBlc();
            long currentPoints = customerWalletDto.getPntBlc();
            long blcAmt;

            // TrdDivCd 랜덤 선택
            String[] trdDivCdOptions = {"MP", "PP"};
            String trdDivCd = trdDivCdOptions[random.nextInt(trdDivCdOptions.length)];

            String[] chrgMeanOptions;
            String pntVldPd = null;
            if ("MP".equals(trdDivCd)) {
                chrgMeanOptions = new String[]{"CA", "ZOZ", "MP"};
                blcAmt = currentMoney;
            } else {
                chrgMeanOptions = new String[]{"CP", "HP"};
                pntVldPd = "20241010";
                blcAmt = currentPoints;
            }
            String chrgMean = chrgMeanOptions[random.nextInt(chrgMeanOptions.length)];
            log.info("chrgMean : []", chrgMean);

            // 2. 충전 (10000)
            long chargeAmount = 10000;  // 테스트용 충전 금액
            ChargeApprovalResponseDto chargeApprovalResponseDto = approvalService.chargeApproval(ChargeApprovalRequestDto.builder()
                    .custNo(custNo)
                    .mTrdNo("test20240910")
                    .divCd(trdDivCd)
                    .chrgMeanCd(chrgMean)
                    .trdAmt(String.valueOf(chargeAmount))
                    .blcAmt(String.valueOf(blcAmt))
                    .pntVldPd(pntVldPd)
                    .customerDto(tgtCustomerDto)
                    .reqDt(curDt)
                    .reqTm(curTm)
                    .trdSumry("API부하테스트")
                    .custBdnFeeAmt("0")
                    .build());

            // 3. 사용 (100원)
            long useAmount = 100;  // 테스트용 사용 금액
            walletService.useWallet(WalletUseRequestDto.builder()
                    .custNo(tgtCustomerDto.getMpsCustNo())
                    .mTrdNo("test20240910")
                    .trdAmt(String.valueOf(useAmount))
                    .mnyBlc(String.valueOf(currentMoney))
                    .pntBlc(String.valueOf(currentPoints))
                    .reqDt(curDt)
                    .reqTm(curTm)
                    .blcUseOrd("P")
                    .customerDto(tgtCustomerDto)
                    .csrcIssReqYn("Y")
                    .stlMId("TEST")
                    .build());

            log.info("사용자 " + customer.getMpsCustNo() + "의 작업 종료 - 스레드: " + Thread.currentThread().getName());
        } catch (Exception e) {
            log.error("에러발생: [{}]", customer.getMpsCustNo());
            e.printStackTrace();
            throw e;
        } finally {
            log.info("남은 작업수: [{}]", latch.getCount());
            latch.countDown(); //작업완료 후 카운트 감소
            log.info("작업완료 후 남은 작업수: [{}]", latch.getCount());
        }

    }
}
