package kr.co.hectofinancial.mps.global.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.RelayReqDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.RelayResDto;
import kr.co.hectofinancial.mps.api.v1.notification.repository.SitePolicyMastRepository;
import kr.co.hectofinancial.mps.global.error.exception.RelayServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class RelayConnectionUtil {
    @Value("${spring.profiles.active}")
    private String profiles;
    private final SitePolicyMastRepository sitePolicyMastRepository;
    private final HttpRequestUtil httpRequestUtil;
    private final ObjectMapper om;

    public RelayResDto sendRequest(String url, Object requestDto) throws RelayServerException {
        log.info("Relay Connection [Start] url={}", url);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");

        RelayReqDto relayReqDto = RelayReqDto.builder()
                .url(url)
                .method(HttpMethod.POST)
                .headers(headers)
                .body(requestDto)
                .build();

        String effectiveProfile = ("local".equals(profiles) || "test".equals(profiles)) ? "tb" : profiles;

        String relayUrl = sitePolicyMastRepository.selectSitePolicy(effectiveProfile + ".mps.relay.api.url");
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String requestBody = null;
        ResponseEntity<RelayResDto> entity = null;
        try {
            requestBody = om.writeValueAsString(relayReqDto);
            log.info("Relay Connection [START] url={} requestBody={}", url, requestBody);
            entity = httpRequestUtil.sendNotiRestRequest(relayUrl, requestBody, MediaType.APPLICATION_JSON_VALUE);
            log.info("Relay Connection [DONE] url={} relayStatus={} apiStatus={} response={}", url, entity.getStatusCode(), entity.getBody().getStatus(), entity.getBody().getBody());
            if (!HttpStatus.OK.equals(entity.getStatusCode())) {
                //relay 서버 자체 오류
                log.error("Relay Server Connection Error :StatusCode={}", entity.getStatusCodeValue());
                throw RelayServerException.relayError(entity.getStatusCodeValue());
            }

            HttpStatus apiStatus = HttpStatus.resolve(Integer.parseInt(entity.getBody().getStatus()));
            if (apiStatus.is5xxServerError()) {
                //relay 타임아웃 혹은 연동서버 50X 에러
                log.error("Relay Server Api Connection Error :StatusCode={}", apiStatus.value());
                throw RelayServerException.apiError(apiStatus.value());
            }
            return entity.getBody();
        } catch (JsonProcessingException e) {
            log.error("Relay Server Json Processing Error", e);
            throw RelayServerException.internalError(e);
        } catch (Exception e) {
            log.error("Relay Server Connection Error", e);
            throw RelayServerException.internalError(e);
        }
    }
}
