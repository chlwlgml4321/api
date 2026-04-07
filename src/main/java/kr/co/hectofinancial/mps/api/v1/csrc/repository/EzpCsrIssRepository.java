package kr.co.hectofinancial.mps.api.v1.csrc.repository;


import kr.co.hectofinancial.mps.api.v1.csrc.domain.EzpCsrcIss;
import kr.co.hectofinancial.mps.api.v1.csrc.domain.EzpCsrcIssPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EzpCsrIssRepository extends JpaRepository<EzpCsrcIss, EzpCsrcIssPK> {

    EzpCsrcIss findByMidAndIssReqNoAndTrdNoAndSvcCdAndPrdtCd(String mId, String issReqNo, String trdNo, String svcCd, String prdtCd);
    EzpCsrcIss findByTrdNoAndIssReqNoAndCnclYnAndSvcCdAndPrdtCd(String trdNo, String issReqNo, String cnclYn, String svcCd, String prdtCd);
    EzpCsrcIss findByOrnTrdNo(String ornTrdNo);
}
