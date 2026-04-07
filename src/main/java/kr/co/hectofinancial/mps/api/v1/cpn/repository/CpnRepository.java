package kr.co.hectofinancial.mps.api.v1.cpn.repository;


import kr.co.hectofinancial.mps.api.v1.cpn.domain.Cpn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CpnRepository extends JpaRepository<Cpn, String> {

    Optional<Cpn> findByCpnId(String cpnId);
}
