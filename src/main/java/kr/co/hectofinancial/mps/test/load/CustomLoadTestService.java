package kr.co.hectofinancial.mps.test.load;

import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import kr.co.hectofinancial.mps.test.feign.FeignClientRequestDto;
import kr.co.hectofinancial.mps.test.feign.FeignClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * TSTDB 의 50명의 회원을 추출하여,
 * 잔액조회 - 충전 - 사용을 50번 반복하는 부하테스트
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomLoadTestService {

    private final FeignClientService feignClientService;
    private List<Long> elapsedTimes = new ArrayList<>();

    public String startLoadTest(int target, int count) {
        //소요시간 평균계산을 위한 리스트 초기화
        elapsedTimes = new ArrayList<>();

        int threadCount = target;
        if (threadCount > 30) {
            threadCount = 30;
        }
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
        List<String> targetCustNos = getTargetCustNo(target);
        for (int i = 0; i < target; i++) {
            String custNo = targetCustNos.get(i % target);
            executor.submit(() -> {
                //테스트 수행
                performTest(count, custNo, executor);

                //남은 작업수 로그 출력
                logThreadPoolStatus(executor);
            });
        }
        executor.shutdown();
        return "OK";
    }

    private List<String> getTargetCustNo(int target) {
        List<String> targetCustNos = new ArrayList<>();
        for (int i = 0; i < target; i++) {
            targetCustNos.add(custNos.get(i % custNos.size()));
        }
        return targetCustNos;
    }

    private void logThreadPoolStatus(ThreadPoolExecutor executor) {
        int activeThreadCount = executor.getActiveCount();
        long completedTaskCount = executor.getCompletedTaskCount();
        long taskCount = executor.getTaskCount();
        int queueSize = executor.getQueue().size();

        log.info("활동스레드 수 : {} 완료된 작업수 : {} 전체 수 : {} ", activeThreadCount, completedTaskCount, taskCount);

    }

    private void performTest(int count, String custNo, ThreadPoolExecutor executor) {
        Random random = new Random();

        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();
        String curTm = customDateTimeUtil.getTime();

        for (int i = 0; i < count; i++) {
            StringBuilder logStr = new StringBuilder();
            try {
//                synchronized (System.out){
                    logStr.append("CustNo:" + custNo + " [" + (i + 1) + "번째] 작업시작 - 스레드:" + Thread.currentThread().getName());
                    long startTime = System.currentTimeMillis();
                    FeignClientRequestDto feignClientRequestDto = new FeignClientRequestDto();
                    feignClientRequestDto.setCustNo(custNo);
                    // 1. 잔액조회
//                    log.info("************ 1. 지갑 조회 시작");
                Map<String, String> walletBalance = (Map<String, String>) feignClientService.getWalletBalance(feignClientRequestDto, "test");
//                    log.info("************ 2. 지갑 조회 종료 {} ", walletBalance);
                    long mnyBlc = Long.parseLong(walletBalance.get("mnyBlc"));
                    long pntBlc = Long.parseLong(walletBalance.get("pntBlc"));
                    logStr.append(" [지갑잔액:" + mnyBlc + "]");


                if (walletBalance != null && walletBalance.keySet().contains("custNo")) {

                    // 2. 충전
                    long chargeAmt = 10000l;
                    feignClientRequestDto.setChrgMeanCd("ZOZ");
                    feignClientRequestDto.setMTrdNo(Thread.currentThread().getName() + "_chrg");
                    feignClientRequestDto.setTrdAmt(String.valueOf(chargeAmt));
                    feignClientRequestDto.setDivCd("MP");
                    feignClientRequestDto.setBlcAmt(String.valueOf(mnyBlc));
                    feignClientRequestDto.setTrdSumry("머니 충전 테스트");
                    feignClientRequestDto.setCustBdnFeeAmt("0");
//                    log.info("************ 3. 충전 시작");
                    Map<String, String> resultMap1 = (Map<String, String>) feignClientService.approvalCharge(feignClientRequestDto, "test");
//                    log.info("************ 4. 충전 종료 {}", resultMap1);
                    String mnyBlc1 = resultMap1.get("mnyBlc");
                    logStr.append(" [충전후:" + mnyBlc1 + "]");

                    if (resultMap1 != null && resultMap1.keySet().contains("custNo")) {

                        // 3. 사용
                        feignClientRequestDto.setMTrdNo(Thread.currentThread().getName() + "_use");
                        feignClientRequestDto.setBlcUseOrd("M");
                        feignClientRequestDto.setMnyBlc(mnyBlc1);
                        feignClientRequestDto.setPntBlc(String.valueOf(pntBlc));
                        feignClientRequestDto.setTrdAmt(mnyBlc1);
                        feignClientRequestDto.setPinNo("190325");
//                        log.info("************ 5. 사용 시작");
                        Map<String, String> resultMap2 = (Map<String, String>) feignClientService.useWallet(feignClientRequestDto, "test");
//                        log.info("************ 6. 사용 종료 {}", resultMap2);
                        logStr.append(" [사용후:" + resultMap2.get("mnyBlc") + "]");
                    }
                }

                long elapsedTimeMills = System.currentTimeMillis() - startTime;
                elapsedTimes.add(elapsedTimeMills);
                logStr.append("소요시간 : " + elapsedTimeMills + "ms" + " [평균소요시간 추이: " + elapsedTimes.stream().mapToDouble(Long::doubleValue).average().orElse(0.0) + "]");
//                }
            } catch (Exception e) {
                logStr.append(" 작업 **실패**");
                e.printStackTrace();
            } finally {
                log.info(logStr.toString());
            }
        }
    }

    private List<String> custNos = Arrays.asList(new String[]{
            "2400021665",
            "2400021681",
            "2400021685",
            "2400021689",
            "2400021693",
            "2400021697",
            "2400021701",
            "2400021705",
            "2400021709",
            "2400021713",
            "2400021717",
            "2400021721",
            "2400021725",
            "2400021729",
            "2400021733",
            "2400021737",
            "2400021741",
            "2400021745",
            "2400021749",
            "2400021753",
            "2400022033",
            "2400022037",
            "2400022041",
            "2400022045",
            "2400022049",
            "2400022053",
            "2400022057",
            "2400022061",
            "2400022065",
            "2400022069",
            "2400022073",
            "2400022077",
            "2400022081",
            "2400022085",
            "2400022089",
            "2400022093",
            "2400022097",
            "2400022101",
            "2400022105",
            "2400022109",
            "2400022113",
            "2400022117",
            "2400022121",
            "2400022125",
            "2400022129",
            "2400022133",
            "2400022137",
            "2400022141",
            "2400022145",
            "2400022149"
    });
}
