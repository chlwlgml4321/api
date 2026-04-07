package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.AdminTrdDtl;
import kr.co.hectofinancial.mps.api.v1.trade.domain.AdminTrdDtlPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AdminTrdDtlRepository extends JpaRepository<AdminTrdDtl, AdminTrdDtlPK>, JpaSpecificationExecutor<AdminTrdDtl> {

    List<AdminTrdDtl> findByTrdReqNoAndRsltStatCd(String trdReqNo, String rsltStatCd);
    List<AdminTrdDtl> findByTrdReqNoAndRsltStatCdAndRmkIsNotNull(String trdReqNo, String rsltStatCd);
}