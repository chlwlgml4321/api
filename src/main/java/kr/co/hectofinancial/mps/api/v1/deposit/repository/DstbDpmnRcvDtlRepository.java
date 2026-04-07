package kr.co.hectofinancial.mps.api.v1.deposit.repository;


import kr.co.hectofinancial.mps.api.v1.deposit.domain.DstbDpmnRcvDtl;
import kr.co.hectofinancial.mps.api.v1.deposit.domain.DstbDpmnRcvDtlPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DstbDpmnRcvDtlRepository extends JpaRepository<DstbDpmnRcvDtl, DstbDpmnRcvDtlPK>, JpaSpecificationExecutor<DstbDpmnRcvDtl> {
    List<DstbDpmnRcvDtl> findByMidAndDpNotiNo(String mid, String dpNotiNo);
    DstbDpmnRcvDtl findTopByMidAndDpNotiNoAndDpAmtOrderByModifiedDateDesc(String mid, String dpNotiNo, long dpAmt);

    DstbDpmnRcvDtl findTopByMidAndDpNotiNoOrderByTrdDt(String mid, String dpNotiNo);
}
