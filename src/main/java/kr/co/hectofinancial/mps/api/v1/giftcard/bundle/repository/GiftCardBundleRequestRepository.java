package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleRequest;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleRequestPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftCardBundleRequestRepository extends JpaRepository<GiftCardBundleRequest, GiftCardBundleRequestPk> {
}
