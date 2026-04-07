package kr.co.hectofinancial.mps.api.v1.cpn.repository;


import kr.co.hectofinancial.mps.api.v1.cpn.domain.CpnM;
import kr.co.hectofinancial.mps.api.v1.cpn.domain.CpnMPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CpnMRepository extends JpaRepository<CpnM, CpnMPK> {

    @Query(value = "select * from BAS.TB_CPN_M m where m.M_ID = :mId and TO_CHAR(SYSDATE, 'YYYYMMDD') between  m.ST_DT and m.ED_DT and ROWNUM <= 1 ", nativeQuery = true)
    Optional<CpnM> findByMid(@Param("mId") String mid);

}
