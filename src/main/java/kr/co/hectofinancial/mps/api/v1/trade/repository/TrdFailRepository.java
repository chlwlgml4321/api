package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.TrdFail;
import kr.co.hectofinancial.mps.api.v1.trade.domain.TrdFailPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TrdFailRepository extends JpaRepository<TrdFail, TrdFailPK>, JpaSpecificationExecutor<TrdFail> {

    List<TrdFail> findTrdFailsBymTrdNoAndFailDtBetweenAndMpsCustNoAndTrdDivCd(String mTrdNo, String startDt, String endDt, String mpsCustNo, String trdDivCd);

}
