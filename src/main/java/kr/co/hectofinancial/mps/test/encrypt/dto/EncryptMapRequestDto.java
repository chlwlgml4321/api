package kr.co.hectofinancial.mps.test.encrypt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EncryptMapRequestDto {

    private String m_id;
    private String uri;
    private Map<String, Object> body;
}
