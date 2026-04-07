package kr.co.hectofinancial.mps.api.v1.deposit.repository;


import kr.co.hectofinancial.mps.api.v1.deposit.domain.DpmnRcv;
import kr.co.hectofinancial.mps.api.v1.deposit.domain.DpmnRcvPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DpmnRcvRepository extends JpaRepository<DpmnRcv, DpmnRcvPK>, JpaSpecificationExecutor<DpmnRcv> {

    List<DpmnRcv> findByMidAndDpStatCdOrderByTrdDt(String mid, String dpStatCd);

    @Query(value = "SELECT * " +
            "FROM ( " +
            "  SELECT * " +
            "  FROM MPS.TB_MPS_DPMN_RCV " +
            "  WHERE 1=1 " +
            "    AND M_ID = :mid " +
            "    AND DP_STAT_CD IN(:fullStatCd, :overStatCd) " +
            "  ORDER BY " +
            "    TRD_DT DESC, " +
            "    CASE WHEN DP_STAT_CD = :overStatCd THEN 1 ELSE 2 END, " +
            "    UPDT_DATE DESC " +
            ") " +
            "WHERE ROWNUM = 1 ", nativeQuery = true)
    DpmnRcv findByDpStatCdOrderByUpdateDtDesc(@Param("mid") String mid, @Param("fullStatCd") String fullStatCd, @Param("overStatCd") String overStatCd);

    @Modifying
    @Query(value = "MERGE INTO MPS.TB_MPS_DPMN_RCV T1 " +
            "USING " +
            "( " +
            "      SELECT T1.ROWID AS RID " +
            "            ,M_ID " +
            "            ,TRD_DT " +
            "            ,DP_REQ_AMT " +
            "            ,DP_AMT " +
            "            ,DP_EXCS_AMT    " +
            "            ,SUM(DP_REQ_AMT) OVER (PARTITION BY M_ID ORDER BY TRD_DT) + V1.CRTR_ACM_DP_REQ_AMT  AS ACM_DP_REQ_AMT " +
            "            ,SUM(DP_AMT) OVER (PARTITION BY M_ID ORDER BY TRD_DT)     + V1.CRTR_ACM_DP_AMT      AS ACM_DP_AMT " +
            "            ,SUM(DP_EXCS_AMT) OVER (PARTITION BY M_ID ORDER BY TRD_DT)+ V1.CRTR_ACM_DP_EXCS_AMT AS ACM_DP_EXCS_AMT " +
            "      FROM MPS.TB_MPS_DPMN_RCV T1 " +
            "          ,( " +
            "            SELECT NVL(MIN(ACM_DP_REQ_AMT),0) AS CRTR_ACM_DP_REQ_AMT " +
            "                  ,NVL(MIN(ACM_DP_AMT),0) AS CRTR_ACM_DP_AMT " +
            "                  ,NVL(MIN(ACM_DP_EXCS_AMT),0) AS CRTR_ACM_DP_EXCS_AMT             " +
            "            FROM MPS.TB_MPS_DPMN_RCV " +
            "            WHERE TRD_DT = (SELECT MAX(TRD_DT) " +
            "                            FROM MPS.TB_MPS_DPMN_RCV " +
            "                            WHERE TRD_DT < :tgtDt  " +
            "                             AND M_ID = :mid                  " +
            "                            ) " +
            "             AND M_ID = :mid  " +
            "          ) V1 " +
            "      WHERE T1.TRD_DT >= :tgtDt  " +
            "        AND T1.M_ID = :mid  " +
            ") V1 " +
            "ON (T1.ROWID = V1.RID) " +
            "WHEN MATCHED THEN " +
            "UPDATE SET ACM_DP_REQ_AMT = V1.ACM_DP_REQ_AMT " +
            "          ,ACM_DP_AMT = V1.ACM_DP_AMT " +
            "          ,ACM_DP_EXCS_AMT = V1.ACM_DP_EXCS_AMT " , nativeQuery = true)
    int updateAcmAmt(@Param("mid") String mid, @Param("tgtDt") String tgtDt);

}
