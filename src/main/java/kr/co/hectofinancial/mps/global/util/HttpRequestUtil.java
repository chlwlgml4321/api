package kr.co.hectofinancial.mps.global.util;

import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.RelayResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class HttpRequestUtil {

	@Qualifier(value = "restTemplateBy50")
	private final RestTemplate restTemplate;	// 디폴트 REST
	
	public ResponseEntity<Map> requestNotiPost (String url, Map<String, String> paramMap) {
		try {
			HttpHeaders header = new HttpHeaders();
			header.setContentType(MediaType.APPLICATION_JSON);
			header.set("Authorization", paramMap.get("authToken"));
			header.set("appServiceSeq", paramMap.get("appServiceSeq"));
			header.set("apiVersion", paramMap.get("apiVersion"));
			HttpEntity<Map<String, String>> entity = new HttpEntity<>(paramMap, header);

			RestTemplate restTemplate = CommonUtil.getRestTemplateInstance(30000, 30000);
			ResponseEntity<Map> resultMap = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
			
			log.info("Sending 'GET' request to URL : " + url + " Response Code : " + resultMap.getStatusCode());
			return resultMap;
		} catch(Exception e) {
			log.error("오류:" + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Relay 서버 노티 발송용
	 *
	 * @param url
	 * @param requestBody
	 * @param contentType
	 * @return
	 */
	public ResponseEntity<RelayResDto> sendNotiRestRequest(String url, Object requestBody, String contentType) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", contentType);

		HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

		RestTemplate restTemplate = CommonUtil.getRestTemplateInstance(50000, 50000);
		return restTemplate.exchange(url, HttpMethod.POST, entity, RelayResDto.class);
	}
}
