package kr.co.hectofinancial.mps.global.error.exception;

import kr.co.hectofinancial.mps.global.extern.whitelabel.constant.WhiteLabelKindCd;
import lombok.Getter;

@Getter
public class WhiteLabelException extends RuntimeException {
    private final WhiteLabelKindCd kind;
    private final boolean causedByValidation;
    private final String statusCd;
    private final String errorCd;
    private final String errorMsg;
    private WhiteLabelException(WhiteLabelKindCd kind, boolean causedByValidation, String statusCd, String errorCd, String errorMsg){
        super(errorMsg);

        this.kind = kind;
        this.causedByValidation = causedByValidation;
        this.statusCd = statusCd;
        this.errorCd = errorCd;
        this.errorMsg = errorMsg;
    }
    private WhiteLabelException(WhiteLabelKindCd kind, boolean causedByValidation, String statusCd, String errorCd, String errorMsg, Throwable e) {
        super(errorMsg, e);

        this.kind = kind;
        this.causedByValidation = causedByValidation;
        this.statusCd = statusCd;
        this.errorCd = errorCd;
        this.errorMsg = errorMsg;
    }

    public static WhiteLabelException preValidation(WhiteLabelKindCd kind, String msg, Throwable e) {
        return new WhiteLabelException(kind, true, null, null, msg, e);
    }
    public static WhiteLabelException preValidation(WhiteLabelKindCd kind, String msg) {
        return new WhiteLabelException(kind, true, null, null, msg);
    }
    public static WhiteLabelException postValidation(WhiteLabelKindCd kind, String msg, Throwable e) {
        return new WhiteLabelException(kind, true, null, null, msg, e);
    }
    public static WhiteLabelException postValidation(WhiteLabelKindCd kind, String msg) {
        return new WhiteLabelException(kind, true, null, null, msg);
    }
    public static WhiteLabelException apiResponseFail(WhiteLabelKindCd kind, String outStatCd, String outRsltCd, String outRsltMsg, Throwable e) {
        return new WhiteLabelException(kind, false, outStatCd, outRsltCd, outRsltMsg, e);
    }
    public static WhiteLabelException apiResponseFail(WhiteLabelKindCd kind, String outStatCd, String outRsltCd, String outRsltMsg) {
        return new WhiteLabelException(kind, false, outStatCd, outRsltCd, outRsltMsg);
    }
    public static WhiteLabelException apiConnectionFail(WhiteLabelKindCd kind, String msg, Throwable e) {
        return new WhiteLabelException(kind, false, null, null, msg, e);
    }
    public static WhiteLabelException apiConnectionFail(WhiteLabelKindCd kind, String msg) {
        return new WhiteLabelException(kind, false, null, null, msg);
    }
    public static WhiteLabelException otherError(WhiteLabelKindCd kind, String msg, Throwable e) {
        return new WhiteLabelException(kind, false, null, null, msg, e);
    }
}