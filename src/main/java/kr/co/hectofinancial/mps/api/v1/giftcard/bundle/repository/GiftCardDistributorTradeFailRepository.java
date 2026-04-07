package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTradeFail;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardDistributorTradeFailPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftCardDistributorTradeFailRepository extends JpaRepository<GiftCardDistributorTradeFail, GiftCardDistributorTradeFailPk> {
}
