package kr.co.hectofinancial.mps.test.encrypt.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EncryptRequestDto {
    private String m_id;//상점아이디
    private String msg;//암,복호화 대상필드

}
