package kr.co.hectofinancial.mps.api.v1.giftcard.single.repository;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardUse;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardUsePk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiftCardUseRepository extends JpaRepository<GiftCardUse, GiftCardUsePk> {

    @Query(value = "SELECT " +
            " T1.TRD_NO " +
            ",T1.TRD_DT " +
            ",T1.TRD_TM " +
            ",CASE WHEN T1.CNCL_YN = 'N' THEN 'U' ELSE 'C' END " +
            ",T1.TRD_SUMRY " +
            ",T1.M_RESRV_FIELD_1 " +
            ",T1.M_RESRV_FIELD_2 " +
            ",T1.M_RESRV_FIELD_3 " +
            "FROM MPS.PM_MPS_GC_USE U1 " +
            "JOIN MPS.PM_MPS_GC_TRD T1 ON U1.TRD_NO = T1.TRD_NO AND U1.TRD_DT = T1.TRD_DT " +
            "WHERE U1.GC_NO_ENC = :gcNoEnc " +
            "  AND U1.ISS_DT = :issDt " +
            "ORDER BY T1.TRD_NO, T1.TRD_DT "
            , nativeQuery = true)
    List<Object[]> findGcUseHistory(@Param("gcNoEnc") String gcNoEnc, @Param("issDt") String issDt);

    GiftCardUse findByGcNoEncAndTrdNoAndTrdDt(@Param("gcNoEnc") String gcNoEnc, @Param("trdNo") String trdNo, @Param("trdDt") String trdDt);
}
