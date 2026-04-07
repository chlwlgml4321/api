package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTrade;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTradePk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftCardDistributorTradeRepository extends JpaRepository<GiftCardDistributorTrade, GiftCardDistributorTradePk> {
    GiftCardDistributorTrade findByDstbTrdNoAndTrdDt(String dstbTrdNo, String trdDt);
    GiftCardDistributorTrade findByOrgDstbTrdNoAndOrgTrdDt(@Param("orgDstbTrdNo") String dstbTrdNo, @Param("orgTrdDt") String trdDt);
}
