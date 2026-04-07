package kr.co.hectofinancial.mps.global.constant;

/**
 * 거래상세조회에서 쓰이는 거래상태코드
 */
public enum StatusCd {
    PROCESSING("0"),    COMPLETED("1");
    private final String StatusCd;

    StatusCd(String statusCd) {
        StatusCd = statusCd;
    }

    public String getStatusCd() {
        return StatusCd;
    }
}
