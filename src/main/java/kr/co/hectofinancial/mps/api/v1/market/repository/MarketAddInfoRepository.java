package kr.co.hectofinancial.mps.api.v1.market.repository;

import kr.co.hectofinancial.mps.api.v1.market.domain.MarketAddInfo;
import kr.co.hectofinancial.mps.api.v1.market.domain.MarketAddInfoPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarketAddInfoRepository extends JpaRepository<MarketAddInfo, MarketAddInfoPK> {
    List<MarketAddInfo> findMarketAddInfoByMid(String mId);
    @Query(value = "select * from BAS.TB_M_ADD_INFO m where m.M_ID = :mId and sysdate between  m.ST_DATE and m.ED_DATE and ROWNUM <= 1 ", nativeQuery = true)
    Optional<MarketAddInfo> findMarketAddInfoByMidAndSysdateBetween(@Param("mId") String mId);
}