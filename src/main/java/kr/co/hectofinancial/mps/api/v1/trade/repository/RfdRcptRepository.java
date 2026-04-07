package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.RfdRcpt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RfdRcptRepository extends JpaRepository<RfdRcpt, String>, JpaSpecificationExecutor<RfdRcpt> {

    RfdRcpt findByRfdRcptNoAndMidAndSvcCdAndRcptDtAndRfdAmt(String rfdRcptNo, String mid, String svcCd, String rcptDt, long rfdAmt);
    List<RfdRcpt> findByMidAndSvcCdAndPrdtCdAndRmk(String mid, String svcCd, String prdtCd, String rmk);
}
