package kr.co.hectofinancial.mps.global.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
public class TransactionLoggerAspect {
    private static final Logger logger = LoggerFactory.getLogger(TransactionLoggerAspect.class);

    // 트랜잭션 롤백 시 로그 출력
    @AfterThrowing(value = "@annotation(org.springframework.transaction.annotation.Transactional)", throwing = "ex")
    public void afterTransactionRollback(JoinPoint joinPoint, Throwable ex) {
        logger.error(">>> Transaction Rollback: {}", joinPoint.getSignature().toShortString());
    }
}
