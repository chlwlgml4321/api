package kr.co.hectofinancial.mps.global.aop;

import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Exception 발생 후, methodArgs Setting 해줌
 * (GlobalExceptionHandler 에서 ErrorCode 를 가지고 MTMS 알람 보내야 할 경우, Setting 한 methodArgs 에서 M_ID, MPS_CUST_NO 등 사용)
 * 
 * Exception 발생하여 실패한 거래의 거래금액 로깅
 */
@Aspect
@Component
@Slf4j
public class GlobalExceptionHandlerAspect {
    private static final String MDC_AMT_ON_ERR_LOGGED = "amtOnErrLogged";

    @AfterThrowing(pointcut = "execution(* kr.co.hectofinancial.mps.api..*(..))", throwing = "ex")
    public void captureException(JoinPoint joinPoint, Throwable ex) {
        Object[] methodArgs = joinPoint.getArgs();

        if (ex instanceof RequestValidationException) {
            RequestValidationException rvEx = (RequestValidationException) ex;
            if (rvEx.getMedthodArgs() == null) {
                rvEx.setMedthodArgs(methodArgs);
            } else {
                Object[] orginalMethodArgs = rvEx.getMedthodArgs();
                Object[] updatedMethodArgs = Stream.concat(Arrays.stream(orginalMethodArgs), Arrays.stream(methodArgs)).toArray(Object[]::new);
                rvEx.setMedthodArgs(updatedMethodArgs);
            }
        }
        if ("Y".equals(MDC.get(MDC_AMT_ON_ERR_LOGGED))) {
            return;
        }
        String kv = ExtractTargetFieldsUtil.extractTargetKVFromArgs(joinPoint.getArgs());
        if (kv.isEmpty()) {
            return;
        }
        // 오류 발생한 거래건(transaction) 의 거래금액 로깅 (extractTargetKVFromArgs 가 "" 아닐경우)
        String name = joinPoint.getSignature().getName();//타겟 메서드명
        String methodName = ex.getStackTrace()[0].getMethodName();//exception 발생한 메서드명

        //FAIL_AMT_LOG , AMT_ON_ERR
        log.error("*** AMT_ON_ERR|src={}.{}|{}", name, methodName, kv);
        // 로깅 후에 amtOnErrLogged 값 Y 로 설정하여, 한트랜잭션 안에서 로깅 한번만 되도록 제어 (안할경우, service -> controller 에러 전파될 때마다 로깅)
        MDC.put(MDC_AMT_ON_ERR_LOGGED, "Y");
        }


}
