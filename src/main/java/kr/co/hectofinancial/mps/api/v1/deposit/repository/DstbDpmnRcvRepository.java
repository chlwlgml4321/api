package kr.co.hectofinancial.mps.api.v1.deposit.repository;


import kr.co.hectofinancial.mps.api.v1.deposit.domain.DstbDpmnRcv;
import kr.co.hectofinancial.mps.api.v1.deposit.domain.DstbDpmnRcvPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DstbDpmnRcvRepository extends JpaRepository<DstbDpmnRcv, DstbDpmnRcvPK>, JpaSpecificationExecutor<DstbDpmnRcv> {

    List<DstbDpmnRcv> findByMidAndDpStatCdOrderByTrdDt(String mid, String dpStatCd);

    @Query(value = "SELECT * " +
            "FROM ( " +
            "  SELECT * " +
            "  FROM MPS.TB_DSTB_DPMN_RCV " +
            "  WHERE 1=1 " +
            "    AND M_ID = :mid " +
            "    AND DP_STAT_CD IN(:fullStatCd, :overStatCd) " +
            "  ORDER BY " +
            "    TRD_DT DESC, " +
            "    CASE WHEN DP_STAT_CD = :overStatCd THEN 1 ELSE 2 END, " +
            "    UPDT_DATE DESC " +
            ") " +
            "WHERE ROWNUM = 1 ", nativeQuery = true)
    DstbDpmnRcv findByDpStatCdOrderByUpdateDtDesc(@Param("mid") String mid, @Param("fullStatCd") String fullStatCd, @Param("overStatCd") String overStatCd);


    @Modifying
    @Query(value = "MERGE INTO MPS.TB_DSTB_DPMN_RCV T1  \n" +
            "USING  \n" +
            "(  \n" +
            "      SELECT T1.ROWID AS RID  \n" +
            "            ,M_ID  \n" +
            "            ,TRD_DT  \n" +
            "            ,DP_REQ_AMT  \n" +
            "            ,DP_AMT  \n" +
            "            ,DP_EXCS_AMT     \n" +
            "            ,SUM(DP_REQ_AMT) OVER (PARTITION BY M_ID ORDER BY TRD_DT) + V1.CRTR_ACM_DP_REQ_AMT  AS ACM_DP_REQ_AMT  \n" +
            "            ,SUM(DP_AMT) OVER (PARTITION BY M_ID ORDER BY TRD_DT)     + V1.CRTR_ACM_DP_AMT      AS ACM_DP_AMT  \n" +
            "            ,SUM(DP_EXCS_AMT) OVER (PARTITION BY M_ID ORDER BY TRD_DT)+ V1.CRTR_ACM_DP_EXCS_AMT AS ACM_DP_EXCS_AMT  \n" +
            "      FROM MPS.TB_DSTB_DPMN_RCV T1  \n" +
            "          ,(  \n" +
            "            SELECT NVL(MIN(ACM_DP_REQ_AMT),0) AS CRTR_ACM_DP_REQ_AMT  \n" +
            "                  ,NVL(MIN(ACM_DP_AMT),0) AS CRTR_ACM_DP_AMT  \n" +
            "                  ,NVL(MIN(ACM_DP_EXCS_AMT),0) AS CRTR_ACM_DP_EXCS_AMT              \n" +
            "            FROM MPS.TB_DSTB_DPMN_RCV \n" +
            "            WHERE TRD_DT = (SELECT MAX(TRD_DT)  \n" +
            "                            FROM MPS.TB_DSTB_DPMN_RCV\n" +
            "                            WHERE TRD_DT < :tgtDt    --제일 처음 더한 날짜\n" +
            "                             AND M_ID = :mid                   \n" +
            "                            )  \n" +
            "             AND M_ID = :mid   \n" +
            "          ) V1  \n" +
            "      WHERE T1.TRD_DT >= :tgtDt   \n" +
            "        AND T1.M_ID = :mid   \n" +
            ") V1  \n" +
            "ON (T1.ROWID = V1.RID)  \n" +
            "WHEN MATCHED THEN  \n" +
            "UPDATE SET ACM_DP_REQ_AMT = V1.ACM_DP_REQ_AMT  \n" +
            "          ,ACM_DP_AMT = V1.ACM_DP_AMT  \n" +
            "          ,ACM_DP_EXCS_AMT = V1.ACM_DP_EXCS_AMT  \n"
            , nativeQuery = true)
    int updateAcmAmt(@Param("mid") String mid, @Param("tgtDt") String tgtDt);

}
