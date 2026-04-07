package kr.co.hectofinancial.mps.api.v1.notification.repository;

import kr.co.hectofinancial.mps.api.v1.notification.domain.PyNtcSend;
import kr.co.hectofinancial.mps.api.v1.notification.domain.PyNtcSendPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PyNtcSendRepository extends JpaRepository<PyNtcSend, PyNtcSendPK> {


}
