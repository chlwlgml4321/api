package kr.co.hectofinancial.mps.global.error.exception;

public class RelayServerException extends RuntimeException {

    private final Integer relayStatus;
    private final Integer apiStatus;
    private final String errorCode;
    public RelayServerException(String message, Integer relayStatus, Integer apiStatus, String errorCode) {
        super(message);
        this.relayStatus = relayStatus;
        this.apiStatus = apiStatus;
        this.errorCode = errorCode;
    }

    public RelayServerException(String message, Throwable cause, Integer relayStatus, Integer apiStatus, String errorCode) {
        super(message, cause);
        this.relayStatus = relayStatus;
        this.apiStatus = apiStatus;
        this.errorCode = errorCode;
    }

    public static RelayServerException relayError(int relayStatus) {
        return new RelayServerException(("relay response fail: statusCode=" + relayStatus), relayStatus, null, "RELAY_ERROR");
    }

    public static RelayServerException apiError(int apiStatus) {
        return new RelayServerException(("api response fail: statusCode=" + apiStatus), 200, apiStatus, "API_ERROR");
    }

    public static RelayServerException internalError(Throwable e) {
        return new RelayServerException(e.getMessage(), e, null, null, "INTERNAL_ERROR");
    }

}
