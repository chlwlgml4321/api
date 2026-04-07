package kr.co.hectofinancial.mps.api.v1.notification.repository;

import kr.co.hectofinancial.mps.api.v1.notification.domain.SitePolicyMast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SitePolicyMastRepository extends JpaRepository<SitePolicyMast, String> {

    @Query(value = "SELECT META_VALUE " +
            "FROM OPM.TB_SITE_POLICY_MAST " +
            "WHERE META_KEY = :metaKey " +
            "AND REALTIME_APPLY_YN = 'Y' ", nativeQuery = true)
    String selectSitePolicy(@Param("metaKey") String metaKey);

//    @Query(value = "SELECT META_KEY " +
//            ", META_VALUE " +
//            ", DESCRIPTION " +
//            ", REALTIME_APPLY_YN " +
//            ", REG_ACCNT_SEQ " +
//            ", REG_DT " +
//            ", UPDATE_ACCNT_SEQ " +
//            ", UPDATE_DT " +
//            ", ROUND(((NVL(UPDATE_DT , REG_DT) + INTERVAL '7' DAY  - SYSDATE) *24 )) AS TOKEN_EXP_DT " +
//            "FROM OPM.TB_SITE_POLICY_MAST " +
//            "WHERE META_KEY = :metaKey " +
//            "AND REALTIME_APPLY_YN = 'Y' ", nativeQuery = true)
//    List<Map<String, Object>> selectSitePolicy(@Param("metaKey") String metaKey);

}
