package kr.co.hectofinancial.mps.api.v1.health.controller;

import kr.co.hectofinancial.mps.api.v1.common.controller.BaseController;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.health.dto.HealthCheckResponseDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.ServerInfo;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * HealthCheck Controller
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/v1")
@Slf4j
public class HealthCheckController extends BaseController {

    private final Environment environment;
    /**
     * HealthCheck
     *
     * @return 서버상태, active profile
     * @author: hyeyoungji
     */
    @GetMapping("/health/check")
    public @ResponseBody ResponseEntity<HealthCheckResponseDto> healthCheck() {
        log.info("** MPS API HEALTH CHECK 진입 => {}", ServerInfoConfig.SERVER_INFO);
        return ResponseEntity.ok(
                HealthCheckResponseDto.builder()
                        .health("ok")
                        .activeProfiles(Arrays.asList(environment.getActiveProfiles()))
                        .activeIps(Arrays.asList(ServerInfoConfig.SERVER_INFO))
                        .build()
        );
    }

    @GetMapping("/health/check/mtms")
    public @ResponseBody ResponseEntity<Object> mtmsHealthCheck() {
        log.info("** MPS MTMS CHECK 진입 => {}", ServerInfoConfig.SERVER_INFO);

        String dateTime = new CustomDateTimeUtil().getDateTime();
        MonitAgent.sendMonitAgent(ErrorCode.MTMS_HEALTH_CHECK_ERROR.getErrorCode(), ErrorCode.MTMS_HEALTH_CHECK_ERROR.getErrorMessage());

        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("mtms global id", dateTime);
        returnMap.put("result", "sendMonitAgent 실행 완료");
        returnMap.put("server info", ServerInfoConfig.SERVER_INFO);

        return ResponseEntity.ok(returnMap);
    }


}
