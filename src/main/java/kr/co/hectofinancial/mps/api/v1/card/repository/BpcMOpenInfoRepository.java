package kr.co.hectofinancial.mps.api.v1.card.repository;

import kr.co.hectofinancial.mps.api.v1.card.domain.BpcMOpenInfo;
import kr.co.hectofinancial.mps.api.v1.card.domain.BpcMOpenInfoPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BpcMOpenInfoRepository extends JpaRepository<BpcMOpenInfo, BpcMOpenInfoPK>, JpaSpecificationExecutor<BpcMOpenInfo> {


    @Query(value = "select * from MPS.TB_BPC_M_OPEN_INFO b where b.M_ID = :mid and b.ORN_ID = :ornId and b.OPEN_STAT_CD = :openStatCd and sysdate between b.ST_DATE and b.ED_DATE AND ROWNUM <= 1 ", nativeQuery = true)
    BpcMOpenInfo findByMidAndOrnIdAndStDateAndEdDate(@Param("mid")String mid, @Param("ornId")String ornId, @Param("openStatCd")String openStatCd);
}
