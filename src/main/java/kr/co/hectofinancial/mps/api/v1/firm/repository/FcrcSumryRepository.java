package kr.co.hectofinancial.mps.api.v1.firm.repository;

import kr.co.hectofinancial.mps.api.v1.firm.domain.FcrcSumry;
import kr.co.hectofinancial.mps.api.v1.firm.domain.FcrcSumryPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FcrcSumryRepository extends JpaRepository<FcrcSumry, FcrcSumryPK>, JpaSpecificationExecutor<FcrcSumry> {

    @Query(value = "select * from BAS.TB_M_PA_DPMN_SUMRY  where ACNT_SUMRY = :acntSumry and BANK_CD = :bankCd and SVC_CD = :svcCd and sysdate between  VLD_ST_DATE and VLD_ED_DATE and ROWNUM <= 1 ", nativeQuery = true)
    FcrcSumry findByAcntSumryAndVldStDateBetween(@Param("acntSumry") String acntSumry, @Param("bankCd") String bankCd, @Param("svcCd") String svcCd);

    @Query(value = "select * from BAS.TB_M_PA_DPMN_SUMRY  where M_ID = :mid and SVC_CD = :svcCd and sysdate between  VLD_ST_DATE and VLD_ED_DATE ", nativeQuery = true)
    FcrcSumry findByMidAndVldStDateBetween(@Param("mid") String mid, @Param("svcCd") String svcCd);
}
