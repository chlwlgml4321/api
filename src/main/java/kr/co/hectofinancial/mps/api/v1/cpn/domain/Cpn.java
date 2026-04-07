package kr.co.hectofinancial.mps.api.v1.cpn.domain;

import kr.co.hectofinancial.mps.api.v1.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_CPN", schema = "BAS")
public class Cpn extends BaseEntity {

    @Id
    @Column(name = "CPN_ID", insertable = false, updatable = false)
    private String cpnId;

    @Column(name = "CPN_NM", insertable = false, updatable = false)
    private String cpnNm;

    @Column(name = "BIZ_DIV_CD", insertable = false, updatable = false)
    private String bizDivCd;

    @Column(name = "TAX_TYPE_CD", insertable = false, updatable = false)
    private String taxTypeCd;

    @Column(name = "BIZ_REG_NO", insertable = false, updatable = false)
    private String bizRegNo;

    @Column(name = "BIZ_TYPE", insertable = false, updatable = false)
    private String bizType;

    @Column(name = "BIZ_ITEM", insertable = false, updatable = false)
    private String bizItem;

    @Column(name = "TEL_NO", insertable = false, updatable = false)
    private String telNo;
    @Column(name = "CPN_URL", insertable = false, updatable = false)
    private String cpnUrl;

}
