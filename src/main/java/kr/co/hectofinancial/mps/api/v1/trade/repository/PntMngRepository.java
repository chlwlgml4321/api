package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.PntMng;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PntMngRepository extends JpaRepository<PntMng, String>, JpaSpecificationExecutor<PntMng> {
    PntMng findByPayHostCd(String payHostCd);
}
