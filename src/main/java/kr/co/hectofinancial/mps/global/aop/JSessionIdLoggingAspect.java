package kr.co.hectofinancial.mps.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

//@Aspect
//@Component
//@Slf4j
public class JSessionIdLoggingAspect {
//todo CachingRequestBodyFilter 안정화 되면 해당 클래스는 삭제예정
    
//	@Before("within(@org.springframework.web.bind.annotation.RestController *)")
//    public void logBeforeRequest() {
//        log.info("[JID-SET] tid={} jid={}",Thread.currentThread().getName(), MDC.get("jsessionId"));
//        byte[] randomBytes = new byte[16];
//		new SecureRandom().nextBytes(randomBytes);
//        MDC.put("jsessionId", Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes));
//    }
//
//	@After("within(@org.springframework.web.bind.annotation.RestController *)")
//    public void logAfterRequest() {
//        log.info("[JID-CLR] tid={} jid={}",Thread.currentThread().getName(), MDC.get("jsessionId"));
//        MDC.remove("jsessionId");
//    }
}
