package kr.co.hectofinancial.mps.api.v1.customer.repository;

import kr.co.hectofinancial.mps.api.v1.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findCustomerByMpsCustNo(String custNo);
    Optional<Customer> findCustomerByMpsCustNoAndMid(String custNo,String mId);

    @Query(value = "select * from mps.tb_mps_cust where m_cust_id = :mCustId and m_id = :mId ", nativeQuery = true)
    Optional<Customer> findCustomerByMCustIdAndMid(@Param("mCustId") String mCustId, @Param("mId") String mId);

    List<Customer> findTop2ByStatCdOrderByCreatedDateAsc(String statCd);

    @Query(value = "SELECT A.MPS_CUST_NO \n" +
            "FROM MPS.TB_MPS_CUST A \n" +
            "JOIN MPS.TB_MPS_M B ON A.M_ID = B.M_ID \n" +
            "WHERE 1=1 \n" +
            "AND A.M_ID = :mid \n" +
            "AND ( A.STAT_CD = 'N' OR (A.STAT_CD = 'L' AND A.BILL_KEY_ENC IS NOT NULL AND B.BILL_KEY_USE_YN='Y' )) \n" +
            "AND A.CUST_NM = :custNm \n" +
            "AND A.CPHONE_NO_ENC= :cPhoneNoEnc \n" +
            "AND A.BIZ_DIV_CD = :bizDivCd ", nativeQuery = true)
    List<String> findByCustNmAndCphoneNoEncAndMidAndBizDivCd(@Param("custNm") String custNm, @Param("cPhoneNoEnc")String cPhoneNoEnc, @Param("mid")String mid, @Param("bizDivCd")String bizDivCd);

    @Query(value = "SELECT MPS_CUST_NO " +
            "FROM MPS.TB_MPS_CUST\n" +
            "WHERE 1=1\n" +
            "AND M_ID = :mid \n" +
            "AND STAT_CD != 'D' \n" +
            "AND CUST_NM = :custNm " +
            "AND CPHONE_NO_ENC= :cPhoneNoEnc " +
            "AND BIZ_DIV_CD = :bizDivCd ", nativeQuery = true)
    List<String> findByCustNmAndCphoneNoEncAndMidAndStatCdNotInAndBizDivCd(@Param("custNm") String custNm, @Param("cPhoneNoEnc")String cPhoneNoEnc, @Param("mid")String mid, @Param("bizDivCd")String bizDivCd);
}

