package kr.co.hectofinancial.mps.api.v1.market.repository;

import kr.co.hectofinancial.mps.api.v1.market.domain.MarketOpenInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MarketOpenInfoRepository extends JpaRepository<MarketOpenInfo, LocalDateTime> {

    @Query(value = "select * from BAS.TB_M_OPEN_INFO m where m.M_ID = :mId and m.SVC_CD = :svcCd and m.PRDT_CD = :prdtCd and m.OPEN_RANGE_CD = :openRangeCd and m.OPEN_STAT_CD = :openStatCd and sysdate between  m.ST_DATE and m.ED_DATE AND ROWNUM <= 1 ", nativeQuery = true)
    Optional<MarketOpenInfo> findMarketOpenInfoByMidAndSvcCdAndPrdtCdAndOpenRangeCdAndStDateBetween(@Param("mId") String mId, @Param("svcCd") String svcCd, @Param("prdtCd") String prdtCd, @Param("openRangeCd") String openRangeCd, @Param("openStatCd")String openStatCd);

    @Query(value = "select * from BAS.TB_M_OPEN_INFO m where m.M_ID = :mId and m.SVC_CD = :svcCd and m.PRDT_CD = :prdtCd and m.OPEN_RANGE_CD = :openRangeCd and sysdate between  m.ST_DATE and m.ED_DATE AND ROWNUM <= 1 ", nativeQuery = true)
    Optional<MarketOpenInfo> findMarketOpenInfoByMidAndSvcCdAndPrdtCdAndOpenRangeCdAndStDateBetween(@Param("mId") String mId, @Param("svcCd") String svcCd, @Param("prdtCd") String prdtCd, @Param("openRangeCd") String openRangeCd);
}
