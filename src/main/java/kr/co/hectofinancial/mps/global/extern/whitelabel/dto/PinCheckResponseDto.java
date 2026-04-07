package kr.co.hectofinancial.mps.global.extern.whitelabel.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PinCheckResponseDto {
    private String outStatCd;

    private String retCd;

    private Long pinErrorCnt;

    private String reqNo;

    public static PinCheckResponseDto parse(String response) {
        int position = 183;
        int len = response.length();

        return PinCheckResponseDto.builder()
                .outStatCd(splitData(response, position, position += 4))
                .retCd(splitData(response, position, position += 4))
                .pinErrorCnt(Long.parseLong(splitData(response, len - 1, len)))
                .build();
    }

    private static String splitData(String response, int position, int length) {
        return response.substring(position, length).trim();
    }

    @Override
    public String toString() {
        return "PinCheckResponseDto{ outStatCd='" + outStatCd + ", retCd='" + retCd + ", pinErrorCnt=" + pinErrorCnt + ", reqNo='" + reqNo + '}';
    }

}
