package kr.co.hectofinancial.mps.api.v1.common.repository;

import kr.co.hectofinancial.mps.api.v1.common.domain.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, String> , JpaSpecificationExecutor<Holiday> {

    Optional<Holiday> findByYmdAndLnrMonDivCd(String tgtDt, String lnrMonDivCd);
}
