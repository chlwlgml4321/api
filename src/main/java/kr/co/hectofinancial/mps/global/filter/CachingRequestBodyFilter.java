package kr.co.hectofinancial.mps.global.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * HttpServletRequest 는 InputStream/Reader 로 읽으면 소모되기 때문에 ,
 * AOP, 인터셉터, 로깅 등에서 바디를 재사용하기 위해 Wrapping
 *
 * 기존의 JSessionIdLoggingAspect 에서 @Before, @After 로 하던 MDC set, remove 를 제일 앞단과 , 하단에 위치 시켜서
 * GlobalExceptionHandler 에서 AfterThrowing 후에도 jsessionId 를 사용할 수 있도록 해당 클래스에서 MDC 관련 작업
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CachingRequestBodyFilter extends OncePerRequestFilter {
    private static final String JSESSION_ID = "jsessionId";
    private static final String MDC_AMT_ON_ERR_LOGGED = "amtOnErrLogged";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //jsonBody 를 여러번 읽을 수 있도록 캐싱해둠
        ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);

        //jsessionId: MDC set
        String existing = MDC.get(JSESSION_ID);
        boolean putHere = false;
        if (existing == null) {
            byte[] randomBytes = new byte[16];
            new SecureRandom().nextBytes(randomBytes);
            String jid = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            MDC.put(JSESSION_ID, jid);
            wrapper.setAttribute(JSESSION_ID, jid);
            putHere = true;
        }
        
        //오류 발생시 거래금액 로깅 관련 amtOnErrLogged 초기화
        MDC.remove(MDC_AMT_ON_ERR_LOGGED);

        try{
            filterChain.doFilter(wrapper, response);
        }finally {
            //jsessionId: MDC remove
            if (putHere) {
                MDC.remove(JSESSION_ID);
                MDC.remove(MDC_AMT_ON_ERR_LOGGED);
            }
        }
    }
}
