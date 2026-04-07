package kr.co.hectofinancial.mps.api.v1.market.repository;

import kr.co.hectofinancial.mps.api.v1.market.domain.MarketServiceProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MarketServiceProductRepository extends JpaRepository<MarketServiceProduct, String> {
    Optional<MarketServiceProduct> findMarketServiceProductByMidAndSvcCdAndPrdtCd(@Param("mid") String mid, @Param("svcCd") String svcCd, @Param("prdtCd") String prdtCd);

    //TODO 현재 사용할때만 해당 쿼리를 사용하므로 PRDT_CD = 'PUSE' 로 고정되어있음 추후 수정
    @Query(value = "SELECT T2.M_ID\n" +
            "            FROM BAS.TB_M_SVC_PRDT T1\n" +
            "            JOIN BAS.TB_DLGT_GRP_MAP T2 ON T1.M_ID    = T2.M_ID\n" +
            "                                       AND T1.SVC_CD  = T2.SVC_CD\n" +
            "                                       AND T1.PRDT_CD = T2.PRDT_CD\n" +
            "            JOIN BAS.TB_DLGT_GRP T3 ON T2.DLGT_GRP_NO = T3.DLGT_GRP_NO\n" +
            "            WHERE 1=1 \n" +
            "            AND T1.M_ID     = :stlMid \n" +
            "            AND T1.SVC_CD     = 'MPS'\n" +
            "            AND T1.PRDT_CD    = 'PUSE'\n" +
            "            AND T2.APL_YN='Y'\n" +
            "            AND TO_CHAR(SYSDATE, 'YYYYMMDD') BETWEEN T2.ST_DT AND T2.ED_DT\n" +
            "            AND T3.PAY_M_ID = :mid \n" +
            "            AND ROWNUM <= 1 ", nativeQuery = true)
    String validStlMid(@Param("stlMid") String stlMid, @Param("mid") String mid);

    @Query(value = "SELECT T2.M_ID\n" +
            "            FROM BAS.TB_M_SVC_PRDT T1\n" +
            "            JOIN BAS.TB_DLGT_GRP_MAP T2 ON T1.M_ID    = T2.M_ID\n" +
            "                                       AND T1.SVC_CD  = T2.SVC_CD\n" +
            "                                       AND T1.PRDT_CD = T2.PRDT_CD\n" +
            "            JOIN BAS.TB_DLGT_GRP T3 ON T2.DLGT_GRP_NO = T3.DLGT_GRP_NO\n" +
            "            WHERE 1=1 \n" +
            "            AND T1.M_ID     = :stlMid \n" +
            "            AND T1.SVC_CD     = 'MPS'\n" +
            "            AND T1.PRDT_CD    = :prdtCd\n" +
            "            AND T2.APL_YN='Y'\n" +
            "            AND TO_CHAR(SYSDATE, 'YYYYMMDD') BETWEEN T2.ST_DT AND T2.ED_DT\n" +
            "            AND T3.PAY_M_ID = :mid \n" +
            "            AND ROWNUM <= 1 ", nativeQuery = true)
    String validStlMid(@Param("stlMid") String stlMid, @Param("mid") String mid, @Param("prdtCd") String prdtCd);

    @Query(value = "SELECT M_ID FROM BAS.TB_M_SVC_PRDT WHERE 1=1 AND PRDT_CD = 'CPIN' AND M_ID = :mid FOR UPDATE WAIT 2 ", nativeQuery = true)
    String lockRowByMid(@Param("mid") String mid);
}
