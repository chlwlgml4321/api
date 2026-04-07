package kr.co.hectofinancial.mps.api.v1.giftcard.single.repository;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardTrade;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardTradePk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftCardTradeRepository extends JpaRepository<GiftCardTrade, GiftCardTradePk> {

    GiftCardTrade findByTrdNoAndTrdDt(@Param("trdNo") String trdNo, @Param("trdDt") String trdDt);
}
