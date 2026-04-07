package kr.co.hectofinancial.mps.global.constant;

public enum AdminFailureType {

    DUPLICATE("DUPLICATE"), HAS_NO_CUST("HAS_NO_CUST"), HAS_CUST("HAS_CUST"), BF_BLC("BF_BLC"), AF_BLC("AF_BLC"), FAIL("FAIL"), SUCCESS("SUCC");
    private final String failureType;

    AdminFailureType(String failureType) {
        this.failureType = failureType;
    }

    public String getFailureType() {
        return this.failureType;
    }
}

