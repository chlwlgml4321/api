package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.UsePnt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UsePntRepository extends JpaRepository<UsePnt, LocalDateTime> {

}
