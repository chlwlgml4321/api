package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.RfdRsltCnf;
import kr.co.hectofinancial.mps.api.v1.trade.domain.RfdRsltCnfPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RfdRsltCnfRepository extends JpaRepository<RfdRsltCnf, RfdRsltCnfPK>, JpaSpecificationExecutor<RfdRsltCnf> {

    @Query(value = "SELECT * " +
            "FROM MPS.TB_MPS_RFD_RSLT_CNF " +
            "WHERE TRD_NO = :orgTrdNo " +
            "AND TRD_DT = :trdDt " +
            "AND RFD_TRD_NO = :rfdTrdNo " +
            "AND MPS_CUST_NO = :custNo " +
            "AND RSLT_CNF_STAT_CD = 'F' " , nativeQuery = true)
    RfdRsltCnf findByRfdTrdNoAndTrdNoAndTrdDt(@Param("rfdTrdNo") String rfdTrdNo, @Param("orgTrdNo") String orgTrdNo, @Param("trdDt") String trdDt, @Param("custNo") String custNo);
}
