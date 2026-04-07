package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelayReqDto {
    private String url;
    private HttpMethod method;
    private Map<String, String> headers;
    private Object body;
}
