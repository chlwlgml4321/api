//package kr.co.hectofinancial.mps.global.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.AsyncConfigurer;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.ThreadPoolExecutor;
//
//@Configuration
//@EnableAsync
//public class AsynConfig implements AsyncConfigurer {
//
//    @Override
//    public Executor getAsyncExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(20); //최소 스레드 수 (대기 없이 처리할 수 있는 수)
//        executor.setMaxPoolSize(50); //최대 스레드 수 (한번에 처리할 수 있는 최대 요청 수)
//        executor.setQueueCapacity(100); //대기 할 수 있는 작업 수
//        // 거부된 작업 처리 핸들러 설정
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); //트래픽 많을 때 거부된 작업을 현재 스레드에서 다시 호출(요청을 버리지 않음)
//        //AbortPolicy는 에러발생
//
//        executor.setThreadNamePrefix("async-thread-");
//        executor.initialize();
//        return executor;
//    }
//}
