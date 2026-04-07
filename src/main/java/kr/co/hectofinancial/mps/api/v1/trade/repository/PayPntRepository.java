package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.PayPnt;
import kr.co.hectofinancial.mps.api.v1.trade.domain.PayPntPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PayPntRepository extends JpaRepository<PayPnt, PayPntPK>, JpaSpecificationExecutor<PayPnt> {

    /* 소멸예정포인트 조회 */
    @Query(value = "SELECT SUM(V2.PNT_AMT + V2.USE_AMT) AS EXPR_AMT " +
            "FROM( " +
            "SELECT T1.LDGR_NO, " +
            "       T1.PNT_AMT, " +
            "       T1.MAKE_DT, " +
            "       T1.CHRG_MEAN_CD, " +
            "                  (SELECT NVL(SUM(USE_AMT * AMT_SIGN), 0) " +
            "                    FROM MPS.PM_MPS_USE_PNT T2 " +
            "                    WHERE T2.LDGR_NO = T1.LDGR_NO " +
            "                      AND T2.MAKE_DT = T1.MAKE_DT " +
            "                   ) USE_AMT " +
            "            FROM " +
            "            ( " +
            "               SELECT MIN(LAST_LDGR_NO) AS LAST_LDGR_NO, " +
            "                     MIN(LAST_MAKE_DT) AS LAST_MAKE_DT " +
            "               FROM MPS.TB_MPS_CUST_WLLT T1 " +
            "               WHERE T1.MPS_CUST_NO = :mpsCustNo " +
            "                 AND T1.BLC_DIV_CD = 'P' " +
            "            ) V1 JOIN MPS.PM_MPS_PAY_PNT T1 ON T1.LDGR_NO >= V1.LAST_LDGR_NO " +
            "                                           AND T1.MAKE_DT >= V1.LAST_MAKE_DT " +
            "                                           AND T1.MPS_CUST_NO = :mpsCustNo                                            " +
            "            WHERE TRIM(T1.VLD_PD) BETWEEN :curDt AND :expDt " +
            "         ) V2 " +
            "         WHERE V2.PNT_AMT + USE_AMT > 0 ", nativeQuery = true)
    Long getExpPntTotal(@Param("mpsCustNo") String custNo, @Param("curDt") String curDt, @Param("expDt") String expDt);

    Optional<PayPnt> findPayPntByPayTrdNoAndPayTrdDtAndMakeDt(String payTrdNo, String payTrdDt, String makeDt);

    @Query(value = "SELECT V2.MPS_CUST_NO " +
            "      ,T1.M_CUST_ID " +
            "      ,V2.VLD_PD " +
            "      ,V2.EXPR_AMT " +
            "FROM " +
            "( " +
            "   SELECT V1.MPS_CUST_NO " +
            "         ,V1.VLD_PD " +
            "         ,SUM(PNT_AMT + USED_AMT) AS EXPR_AMT " +
            "   FROM " +
            "   ( " +
            "      SELECT T1.MPS_CUST_NO " +
            "            ,T1.LDGR_NO " +
            "            ,T1.VLD_PD " +
            "            ,T1.PNT_AMT " +
            "            ,NVL(SUM(T2.USE_AMT * T2.AMT_SIGN),0) AS USED_AMT " +
            "      FROM MPS.PM_MPS_PAY_PNT T1 " +
            "      LEFT JOIN MPS.PM_MPS_USE_PNT T2 ON T1.LDGR_NO = T2.LDGR_NO " +
            "                                     AND T1.MAKE_DT = T2.MAKE_DT " +
            "      WHERE 1=1 " +
            "        AND T1.VLD_PD = :tgtDt " +
            "        AND T1.MAKE_DT BETWEEN TO_CHAR(SYSDATE-400, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD') /* 포인트 최대 유효기간 */ " +
            "        AND T1.M_ID = :mid " +
            "      GROUP BY T1.MPS_CUST_NO " +
            "              ,T1.LDGR_NO " +
            "              ,T1.VLD_PD " +
            "              ,T1.PNT_AMT " +
            "   ) V1 " +
            "   GROUP BY V1.MPS_CUST_NO " +
            "           ,V1.VLD_PD " +
            "   HAVING SUM(PNT_AMT + USED_AMT) > 0 " +
            "   ORDER BY 1,2 " +
            ") V2 JOIN MPS.TB_MPS_CUST T1 ON V2.MPS_CUST_NO = T1.MPS_CUST_NO ", nativeQuery = true)
    List<Object[]> getExpPntAmtByMid(@Param("mid") String mid, @Param("tgtDt") String tgtDt); //TODO 포인트 최대유효기간이 1년기준이었을 때 쿼리

    @Query(value = "SELECT PNT_AMT + NVL(USED_AMT,0) AS PNT_BLC\n" +
            "FROM\n" +
            "(      \n" +
            "   SELECT PNT_AMT\n" +
            "        ,(SELECT SUM(USE_AMT * AMT_SIGN)\n" +
            "          FROM MPS.PM_MPS_USE_PNT T2\n" +
            "          WHERE T2.LDGR_NO = T1.LDGR_NO\n" +
            "            AND T2.MAKE_DT = T1.MAKE_DT\n" +
            "          ) AS USED_AMT   \n" +
            "   FROM MPS.PM_MPS_PAY_PNT T1\n" +
            "   WHERE MAKE_DT >= :trdDt\n" +
            "    AND PAY_TRD_NO = :trdNo\n" +
            "    AND PAY_TRD_DT = :trdDt\n" +
            "    AND ROWNUM <= 1\n" +
            ") ", nativeQuery = true)
    long getPntBlcByLedger(@Param("trdDt") String trdDt, @Param("trdNo") String trdNo);

}