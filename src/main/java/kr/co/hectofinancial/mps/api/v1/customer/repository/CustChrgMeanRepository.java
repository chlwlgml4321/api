package kr.co.hectofinancial.mps.api.v1.customer.repository;

import kr.co.hectofinancial.mps.api.v1.customer.domain.CustChrgMean;
import kr.co.hectofinancial.mps.api.v1.customer.domain.CustChrgMeanPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustChrgMeanRepository extends JpaRepository<CustChrgMean, CustChrgMeanPK>, JpaSpecificationExecutor<CustChrgMean> {

    Optional<CustChrgMean> findByMpsCustNoAndChrgMeanCd(String custNo, String chrgMeanCd);

}
