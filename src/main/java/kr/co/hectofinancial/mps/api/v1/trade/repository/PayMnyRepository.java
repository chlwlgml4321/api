package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.PayMny;
import kr.co.hectofinancial.mps.api.v1.trade.domain.PayMnyPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PayMnyRepository extends JpaRepository<PayMny, PayMnyPK>, JpaSpecificationExecutor<PayMny> {

    PayMny findByLdgrNoAndMpsCustNo(String ldgrNo, String custNo);


    @Query(value = "SELECT MNY_AMT + NVL(USED_AMT,0) AS MNY_BLC\n" +
            "FROM\n" +
            "(      \n" +
            "   SELECT MNY_AMT\n" +
            "        ,(SELECT SUM(USE_AMT * AMT_SIGN)\n" +
            "          FROM MPS.PM_MPS_USE_MNY T2\n" +
            "          WHERE T2.LDGR_NO = T1.LDGR_NO\n" +
            "            AND T2.MAKE_DT = T1.MAKE_DT\n" +
            "          ) AS USED_AMT   \n" +
            "   FROM MPS.PM_MPS_PAY_MNY T1\n" +
            "   WHERE MAKE_DT >= :trdDt\n" +
            "    AND PAY_TRD_NO = :trdNo\n" +
            "    AND PAY_TRD_DT = :trdDt\n" +
            "    AND ROWNUM <= 1\n" +
            ") ", nativeQuery = true)
   long getMnyBlcByLedger(@Param("trdDt") String trdDt, @Param("trdNo") String trdNo);
}
