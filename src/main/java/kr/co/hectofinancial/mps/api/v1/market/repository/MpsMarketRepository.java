package kr.co.hectofinancial.mps.api.v1.market.repository;

import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MpsMarketRepository extends JpaRepository<MpsMarket, String> {

    Optional<MpsMarket> findMpsMarketByMid(String mId);

    @Query(value = "SELECT M_ID FROM MPS.TB_MPS_M WHERE 1=1 AND M_ID = :mid FOR UPDATE WAIT 2 ", nativeQuery = true)
    String lockRowByMid(@Param("mid") String mid);
}
