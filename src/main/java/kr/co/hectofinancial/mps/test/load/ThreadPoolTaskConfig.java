package kr.co.hectofinancial.mps.test.load;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class ThreadPoolTaskConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "async")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); //최소 스레드 수 (대기 없이 처리할 수 있는 수)
        executor.setMaxPoolSize(50); //최대 스레드 수 (한번에 처리할 수 있는 최대 요청 수)
        executor.setQueueCapacity(10000); //대기 할 수 있는 작업 수
        executor.setTaskDecorator(new ContextCopyingDecorator());  // 컨텍스트 복사 설정
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;

    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return ((throwable, method, objects) -> {
            log.error("***Exception in AsyncService Method : => [{}], [{}]", method.getName(), throwable.getMessage());
            throwable.printStackTrace();
        });
    }

    // 요청 컨텍스트를 복사하는 TaskDecorator
    public class ContextCopyingDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            return () -> {
                try {
                    RequestContextHolder.setRequestAttributes(context);
                    runnable.run();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        }
    }
}
