package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants;

public class GcBndlConstants {

    public static final int maxIssCntOnServer = 1000; // 서버 최대 발행 건수. 초과 시 배치 처리
    public static final int maxIssReqCnt = 5000; // 최대 발행 요청 건수. 초과 시 오류
}
