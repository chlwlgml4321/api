package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundleDistributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

@Repository
public interface GiftCardBundleDistributorRepository extends JpaRepository<GiftCardBundleDistributor, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "0") // NO WAIT
    })
    GiftCardBundleDistributor findByGcDstbNo(String gcDstbNo);

    @Query(value = "SELECT * FROM MPS.TB_MPS_BNDL_DSTB WHERE GC_DSTB_NO = :gcDstbNo", nativeQuery = true)
    GiftCardBundleDistributor findByGcDstbNoForReadOnly(@Param("gcDstbNo") String gcDstbNo);
}
