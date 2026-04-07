package kr.co.hectofinancial.mps.api.v1.card.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "TB_BPC_CUST", schema = "MPS")
public class BpcCust {

    @Id
    @Column(name = "CARD_MNG_NO")
    private String cardMngNo;
    @Column(name = "CARD_NO_MSK")
    private String cardNoMsk;
    @Column(name = "CARD_NO_ENC")
    private String cardNoEnc;
    @Column(name = "MPS_CUST_NO")
    private String mpsCustNo;
    @Column(name = "ORN_ID")
    private String ornId;
    @Column(name = "M_ID")
    private String mid;
    @Column(name = "PTN_PRDT_NO")
    private String ptnPrdtNo;
    @Column(name = "LAST_CARD_DIV_CD")
    private String lastCardDivCd;
    @Column(name = "CARD_STAT_CD")
    private String cardStatCd;
    @Column(name = "SELF_DLV_YN")
    private String selfDlvYn;
    @Column(name = "DMST_PMT_BLK_YN")
    private String dmstPmtBlkYn;

}
