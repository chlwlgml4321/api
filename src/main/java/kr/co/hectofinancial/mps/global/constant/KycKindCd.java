package kr.co.hectofinancial.mps.global.constant;

public enum KycKindCd {
    CDD("1"),
    EDD("2");
    private final String kycKindCd;

    KycKindCd(String kycKindCd) {
        this.kycKindCd = kycKindCd;
    }

    public String getKycKindCd() {
        return kycKindCd;
    }
}
