package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.AdminTrdReq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AdminTrdReqRepository extends JpaRepository<AdminTrdReq, String>, JpaSpecificationExecutor<AdminTrdReq> {

    AdminTrdReq findByTrdReqNoAndProcStatCdInAndMid(String trdReqNo, List<String> procStatCdList, String mid);

}
