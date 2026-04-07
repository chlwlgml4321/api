package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.TrdDivDtl;
import kr.co.hectofinancial.mps.api.v1.trade.domain.TrdDivDtlPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TrdDivDtlRepository extends JpaRepository<TrdDivDtl, TrdDivDtlPK>, JpaSpecificationExecutor<TrdDivDtl> {

    TrdDivDtl findByMidAndTrdDivCdAndTrdDivDtlCdAndUseYn(String mid, String trdDivCd, String trdDivDtlCd, String useYn);
}
