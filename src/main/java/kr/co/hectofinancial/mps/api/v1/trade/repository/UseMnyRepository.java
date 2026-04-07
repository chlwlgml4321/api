package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.UseMny;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UseMnyRepository extends JpaRepository<UseMny, LocalDateTime>, JpaSpecificationExecutor<UseMny> {

    @Query(value = "SELECT * " +
            "FROM MPS.PM_MPS_USE_MNY " +
            "WHERE USE_TRD_NO = :useTrdNo ", nativeQuery = true)
    List<UseMny> findByUseTrdNo(@Param("useTrdNo") String useTrdNo);
}
