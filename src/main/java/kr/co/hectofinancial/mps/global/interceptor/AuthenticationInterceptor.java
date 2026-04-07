package kr.co.hectofinancial.mps.global.interceptor;

import com.google.common.util.concurrent.RateLimiter;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 1. 초당 수용 요청은 최대 100건
 */
@Component
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter;

    public AuthenticationInterceptor() {
        this.rateLimiter = RateLimiter.create(200);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String remoteAddr = getRemoteAddr(request);
        if (!rateLimiter.tryAcquire()) {
            throw new RequestValidationException(ErrorCode.TOO_MANY_REQUESTS);
        }
        log.info("REQUEST IP => [{}]", remoteAddr);
        return true;
    }

    private String getRemoteAddr(HttpServletRequest request) {
        return (null != request.getHeader("X-FORWARDED-FOR")) ? request.getHeader("X-FORWARDED-FOR") : request.getRemoteAddr();
    }

}
