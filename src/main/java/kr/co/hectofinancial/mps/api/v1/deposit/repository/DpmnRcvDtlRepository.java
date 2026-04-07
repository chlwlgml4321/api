package kr.co.hectofinancial.mps.api.v1.deposit.repository;


import kr.co.hectofinancial.mps.api.v1.deposit.domain.DpmnRcvDtl;
import kr.co.hectofinancial.mps.api.v1.deposit.domain.DpmnRcvDtlPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DpmnRcvDtlRepository extends JpaRepository<DpmnRcvDtl, DpmnRcvDtlPK>, JpaSpecificationExecutor<DpmnRcvDtl> {
    List<DpmnRcvDtl> findByMidAndDpNotiNo(String mid, String dpNotiNo);
    DpmnRcvDtl findTopByMidAndDpNotiNoAndDpAmtOrderByModifiedDateDesc(String mid, String dpNotiNo, long dpAmt);
    DpmnRcvDtl findTopByMidAndDpNotiNoOrderByTrdDt(String mid, String dpNotiNo);
}
