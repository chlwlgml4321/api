package kr.co.hectofinancial.mps.api.v1.giftcard.single.repository;

import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssue;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.domain.GiftCardIssuePk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GiftCardIssueRepository extends JpaRepository<GiftCardIssue, GiftCardIssuePk> {

    /**
     * 상품권 사용 시
     * @param gcNoEncList
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "0") // NO WAIT
    })
    List<GiftCardIssue> findByGcNoEncIn(List<String> gcNoEncList);

    /**
     * 상품권 재발행 또는 사용취소 시
     * @param gcNoEnc
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "0") // NO WAIT
    })
    GiftCardIssue findByGcNoEnc(String gcNoEnc);

    GiftCardIssue findByBfGcNoEncAndBfIssDt(String bfGcNoEnc, String bfIssDt);

    @Query(value = "SELECT " +
            " GC_NO_ENC " +
            ",ISS_DT " +
            ",GC_NO_MSK " +
            ",ISS_TRD_NO " +
            ",ISS_AMT " +
            ",BLC " +
            ",VLD_PD " +
            ",USE_M_ID " +
            ",GC_STAT_CD " +
            ",BF_GC_NO_ENC " +
            ",BF_ISS_DT " +
            ",RMK " +
            ",INST_DATE " +
            ",INST_IP " +
            ",INST_ID " +
            ",UPDT_DATE " +
            ",UPDT_IP " +
            ",UPDT_ID " +
            "FROM MPS.PM_MPS_GC_ISS " +
            "WHERE GC_NO_ENC = :gcNoEnc "
            , countQuery = "SELECT COUNT(1) FROM MPS.PM_MPS_GC_ISS WHERE GC_NO_ENC = :gcNoEnc "
            , nativeQuery = true)
    Page<GiftCardIssue> findGiftCardIssueList(@Param("gcNoEnc") String gcNoEnc, Pageable pageable); // 상품권 조회

    @Query(value = "SELECT " +
            " GC_NO_ENC " +
            ",ISS_DT " +
            ",GC_NO_MSK " +
            ",ISS_TRD_NO " +
            ",ISS_AMT " +
            ",BLC " +
            ",VLD_PD " +
            ",USE_M_ID " +
            ",GC_STAT_CD " +
            ",BF_GC_NO_ENC " +
            ",BF_ISS_DT " +
            ",RMK " +
            ",INST_DATE " +
            ",INST_IP " +
            ",INST_ID " +
            ",UPDT_DATE " +
            ",UPDT_IP " +
            ",UPDT_ID " +
            "FROM MPS.PM_MPS_GC_ISS " +
            "WHERE GC_NO_ENC = :gcNoEnc" +
            "  AND ISS_DT = :issDt "
            , countQuery = "SELECT COUNT(1) FROM MPS.PM_MPS_GC_ISS WHERE GC_NO_ENC = :gcNoEnc AND ISS_DT = :issDt  "
            , nativeQuery = true)
    Page<GiftCardIssue> findByGcNoEncAndIssDt(@Param("gcNoEnc") String gcNoEnc, @Param("issDt") String issDt, Pageable pageable); // 상품권 조회

    @Query(value = "SELECT " +
            " GC_NO_ENC " +
            ",ISS_DT " +
            ",GC_NO_MSK " +
            ",ISS_TRD_NO " +
            ",ISS_AMT " +
            ",BLC " +
            ",VLD_PD " +
            ",USE_M_ID " +
            ",GC_STAT_CD " +
            ",BF_GC_NO_ENC " +
            ",BF_ISS_DT " +
            ",RMK " +
            ",INST_DATE " +
            ",INST_IP " +
            ",INST_ID " +
            ",UPDT_DATE " +
            ",UPDT_IP " +
            ",UPDT_ID " +
            "FROM MPS.PM_MPS_GC_ISS " +
            "WHERE ISS_TRD_NO = :gcIssTrdNo "
            , countQuery = "SELECT COUNT(1) FROM MPS.PM_MPS_GC_ISS WHERE ISS_TRD_NO = :gcIssTrdNo "
            , nativeQuery = true)
    Page<GiftCardIssue> findByGcIssTrdNo(@Param("gcIssTrdNo") String gcIssTrdNo, Pageable pageable); // 상품권 조회

    @Query(value = "SELECT " +
            " GC_NO_ENC " +
            ",ISS_DT " +
            ",GC_NO_MSK " +
            ",ISS_TRD_NO " +
            ",ISS_AMT " +
            ",BLC " +
            ",VLD_PD " +
            ",USE_M_ID " +
            ",GC_STAT_CD " +
            ",BF_GC_NO_ENC " +
            ",BF_ISS_DT " +
            ",RMK " +
            ",INST_DATE " +
            ",INST_IP " +
            ",INST_ID " +
            ",UPDT_DATE " +
            ",UPDT_IP " +
            ",UPDT_ID " +
            "FROM MPS.PM_MPS_GC_ISS " +
            "WHERE ISS_TRD_NO = :gcIssTrdNo " +
            "  AND ISS_DT = :issDt "
            , countQuery = "SELECT COUNT(1) FROM MPS.PM_MPS_GC_ISS WHERE ISS_TRD_NO = :gcIssTrdNo AND ISS_DT = :issDt "
            , nativeQuery = true)
    Page<GiftCardIssue> findByGcIssTrdNoAndIssDt(@Param("gcIssTrdNo") String gcIssTrdNo, @Param("issDt") String issDt, Pageable pageable); // 상품권 조회

    int countByIssDtAndBndlPinNoEncAndBndlPinIssDt(String issDt, String bndlPinNoEnc, String bndlPinIssDt);
}
