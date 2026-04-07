package kr.co.hectofinancial.mps.api.v1.giftcard.bundle.repository;

import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundlePin;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.domain.GiftCardBundlePinPk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

@Repository
public interface GiftCardBundlePinRepository extends JpaRepository<GiftCardBundlePin, GiftCardBundlePinPk> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "0") // NO WAIT
    })
    GiftCardBundlePin findByBndlPinNoEnc(String bndlPinNoEnc);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "0") // NO WAIT
    })
    GiftCardBundlePin findByBndlPinNoEncAndIssDt(String bndlPinNoEnc, String issDt);

    @Query(value = "SELECT * FROM MPS.PM_MPS_GC_BNDL_PIN WHERE BNDL_PIN_NO_ENC = :bndlPinNoEnc", nativeQuery = true)
    GiftCardBundlePin findByBndlPinNoEncForReadOnly(@Param("bndlPinNoEnc") String bndlPinNoEnc);

    @Query(value = "SELECT * FROM MPS.PM_MPS_GC_BNDL_PIN WHERE BNDL_PIN_NO_ENC = :bndlPinNoEnc AND ISS_DT = :issDt", nativeQuery = true)
    GiftCardBundlePin findByBndlPinNoEncAndIssDtForReadOnly(@Param("bndlPinNoEnc") String bndlPinNoEnc, @Param("issDt") String issDt);

    @Query(value = "SELECT " +
            " BNDL_PIN_NO_ENC " +
            ",ISS_DT " +
            ",BNDL_PIN_NO_MSK " +
            ",M_ID " +
            ",GC_DSTB_NO " +
            ",BNDL_AMT " +
            ",BNDL_REQ_DTM " +
            ",BNDL_CMPLT_DTM " +
            ",VLD_PD " +
            ",PIN_STAT_CD " +
            ",BNDL_REQ_INFO " +
            ",BNDL_REQ_NO " +
            ",BNDL_REQ_DT " +
            ",RMK " +
            ",INST_DATE " +
            ",INST_IP " +
            ",INST_ID " +
            ",UPDT_DATE " +
            ",UPDT_IP " +
            ",UPDT_ID " +
            "FROM MPS.PM_MPS_GC_BNDL_PIN " +
            "WHERE GC_DSTB_NO = :gcDstbNo " +
            "ORDER BY ISS_DT DESC "
            , countQuery = "SELECT COUNT(1) FROM MPS.PM_MPS_GC_BNDL_PIN WHERE GC_DSTB_NO = :gcDstbNo "
            , nativeQuery = true)
    Page<GiftCardBundlePin> findAllByGcDstbNo(@Param("gcDstbNo") String gcDstbNo, Pageable pageable);
}
