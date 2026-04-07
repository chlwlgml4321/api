package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.repository;

import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain.NotiInfo;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain.NotiInfoPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotiInfoRepository extends JpaRepository<NotiInfo, NotiInfoPK>, JpaSpecificationExecutor<NotiInfo> {

    @Query(value = "select * from MPS.TB_MPS_NOTI_INFO n where n.M_ID = :mId and n.NOTI_TYPE_CD = :notiTypeCd and n.USE_YN = :useYn and sysdate between n.ST_DATE and n.ED_DATE AND ROWNUM <= 1 ", nativeQuery = true)
    NotiInfo findByMidAndNotiTypeCdAndUseYnAndStDateAndEdDate(@Param("mId")String mId, @Param("notiTypeCd")String notiTypeCd, @Param("useYn")String useYn);
}
