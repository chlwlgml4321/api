package kr.co.hectofinancial.mps.api.v1.market.repository;

import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMaketChrgMapPK;
import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarketChrgMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MpsMarketChrgMapRepository extends JpaRepository<MpsMarketChrgMap, MpsMaketChrgMapPK> {

    @Query(value = "select m.*, tc.CD_NM as CHRG_MEAN_NM from MPS.TB_MPS_M_CHRG_MAP m " +
            "join BAS.TB_CD tc on m.CHRG_MEAN_CD = tc.CD and tc.CD_GRP_ID = 'MPS_CHRG_MEAN_CD' " +
            "where m.M_ID = :mid and m.DISP_YN = 'Y' and sysdate between m.ST_DATE and m.ED_DATE order by m.DISP_ORD asc, m.INST_DATE asc ",nativeQuery = true)
    List<Map<String,Object>> findAllByMid(@Param("mid") String mid);

    @Query(value = "SELECT CD_NM as CHRG_MEAN_NM " +
            "FROM BAS.TB_CD " +
            "WHERE 1=1 " +
            "AND CD_GRP_ID = 'MPS_CHRG_MEAN_CD' " +
            "and CD = :chrgMeanCd ",nativeQuery = true)
    String findByMidAndChrgMeanCd(@Param("chrgMeanCd") String chrgMeanCd);

    @Query(value = "select * " +
            "from MPS.TB_MPS_M_CHRG_MAP " +
            "where M_ID = :mid " +
            "AND CHRG_MEAN_CD = :chrgMeanCd " +
            "AND WLT_TYPE_DIV_CD = :wltTypeDivCd " +
            "AND SYSDATE " +
            "between ST_DATE and ED_DATE and ROWNUM <=1 " , nativeQuery = true)
    Optional<MpsMarketChrgMap> findAllByMidAndChrgMeanCd(@Param("mid") String mid, @Param("chrgMeanCd") String chrgMeanCd, @Param("wltTypeDivCd") String wltTypeDivCd);

    @Query(value = "select * " +
            "from MPS.TB_MPS_M_CHRG_MAP " +
            "where M_ID = :mid " +
            "and CHRG_MEAN_CD = :chrgMeanCd " +
            "and :orgDt between ST_DATE and ED_DATE  and ROWNUM <=1 " , nativeQuery = true)
    MpsMarketChrgMap findByChrgCnclPsblYn(@Param("mid") String mid, @Param("chrgMeanCd") String chrgMeanCd, @Param("orgDt") String orgDt);

}