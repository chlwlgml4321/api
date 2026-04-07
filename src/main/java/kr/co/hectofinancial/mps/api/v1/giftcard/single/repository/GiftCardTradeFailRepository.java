package kr.co.hectofinancial.mps.api.v1.giftcard.single.repository;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardTradeFail;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardTradeFailPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftCardTradeFailRepository extends JpaRepository<GiftCardTradeFail, GiftCardTradeFailPk> {
}
