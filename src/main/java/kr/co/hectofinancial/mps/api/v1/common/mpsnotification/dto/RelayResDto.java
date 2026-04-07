package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

@Getter
@Setter
public class RelayResDto {
    private String status;
    private Object body;

}
