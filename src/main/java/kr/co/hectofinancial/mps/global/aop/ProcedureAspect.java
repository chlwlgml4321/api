package kr.co.hectofinancial.mps.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * kr.co.hectofinancial.mps.api.v1.trade.procedure...하위의 Procedure 수행 후 작동하는 AOP 로
 * 프로시저 수행 후, 강제로 예외를 발생시켜서 rollback 기능 테스트때 사용
 * profile = local 로 설정하면 로컬환경일때만 동작함
 */
@Profile({"local"})
@Slf4j
@Component
@Aspect
public class ProcedureAspect {
//    @AfterReturning("execution(* kr.co.hectofinancial.mps.api.v1.trade.procedure.Pay..*(..)) || execution(* kr.co.hectofinancial.mps.api.v1.trade.procedure.Use..*(..))")
//    public void afterPayUseProcedureCall() {
//        log.info("ProcedureAspect 를 통해서 오라클 프로시저 호출 후 예외 발생");
//        throw new RuntimeException("Exception After Oracle Procedure call");
//    }

//    @AfterReturning("execution(* kr.co.hectofinancial.mps.api.v1.trade.procedure.PayCancel..*(..)) || execution(* kr.co.hectofinancial.mps.api.v1.trade.procedure.UseCancel..*(..))")
//    public void afterPayUseCancelProcedureCall() {
//        log.info("ProcedureAspect 를 통해서 **취소 관련** 오라클 프로시저 호출 후 예외 발생");
//        throw new RuntimeException("Exception After Oracle Procedure call");
//    }

//    @AfterReturning("execution(* kr.co.hectofinancial.mps.api.v1.trade.procedure.Withdrawal..*(..))")
//    public void afterWithdrawProcedureCall() {
//        log.info("ProcedureAspect 를 통해서 **출금** 오라클 프로시저 호출 후 예외 발생");
//        throw new RuntimeException("Exception After Oracle Procedure call");
//    }
}
