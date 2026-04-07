package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.repository;

import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain.NotiInfo;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain.NotiSend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotiSendRepository extends JpaRepository<NotiSend, String>, JpaSpecificationExecutor<NotiInfo> {

}
