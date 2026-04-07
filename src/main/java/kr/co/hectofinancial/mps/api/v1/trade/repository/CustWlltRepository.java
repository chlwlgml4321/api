package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.CustWllt;
import kr.co.hectofinancial.mps.api.v1.trade.domain.CustWlltPK;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlc.GetBlc;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.GetMWAmt.GetMWAmt;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.PntExpr.PntExpr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CustWlltRepository extends JpaRepository<CustWllt, CustWlltPK>, JpaSpecificationExecutor<CustWllt>, GetBlc, GetMWAmt, PntExpr {

    List<CustWllt> findByMpsCustNo(String custNo);
}
