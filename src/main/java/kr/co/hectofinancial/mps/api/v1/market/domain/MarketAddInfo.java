package kr.co.hectofinancial.mps.api.v1.market.domain;

import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_M_ADD_INFO", schema = "BAS")
@IdClass(MarketAddInfoPK.class)
public class MarketAddInfo {

    @Id
    @Column(name = "M_ID", insertable = false, updatable = false)
    private String mid;

    @Id
    @Column(name = "ST_DATE", insertable = false, updatable = false)
    private LocalDateTime stDate;

    @Column(name = "ED_DATE", insertable = false, updatable = false)
    private LocalDateTime edDate;

    @Column(name = "ENC_USE_YN", insertable = false, updatable = false)
    private String encUseYn; //암호화키 사용여부

    @Column(name = "ENC_KEY", insertable = false, updatable = false)
    @Convert(converter = DatabaseAESCryptoUtil.class)
    private String encKey;

    @Column(name = "ENC_IV", insertable = false, updatable = false)
    @Convert(converter = DatabaseAESCryptoUtil.class)
    private String encIv;

    @Column(name = "ENC_MTHD_CD", insertable = false, updatable = false)
    private String encMthdCd;

    @Column(name = "PKT_HASH_KEY", insertable = false, updatable = false)
    private String pktHashKey;

}
