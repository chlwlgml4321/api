package kr.co.hectofinancial.mps.global.config.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class JpaAuditorAwareImpl implements AuditorAware<String> {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public Optional<String> getCurrentAuditor() {
        String requestURI = httpServletRequest.getRequestURI();
        if (!StringUtils.hasText(requestURI)) {
            requestURI = "unknown";
        }
        return Optional.of(requestURI);
    }
}
