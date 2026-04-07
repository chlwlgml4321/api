package kr.co.hectofinancial.mps.api.v1.firm.repository;

import kr.co.hectofinancial.mps.api.v1.firm.domain.DpmnNoti;
import kr.co.hectofinancial.mps.api.v1.firm.domain.DpmnNotiPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DpmnNotiRepository extends JpaRepository<DpmnNoti, DpmnNotiPK> {

    DpmnNoti findByGlobalIdAndTrdDtAndOutRsltCdAndSvcCdAndVanIsttCd(String globalId, String trdDt, String outRsltCd, String svcCd, String vanIsttCd);

    DpmnNoti findBySvcCdAndVanIsttCdAndOrnIdAndOrnPktNoAndOrnPktDivCdAndOrnJobDivCdAndTrdDtAndTrdTmAndTrdAmtAndNotiClssCd(String svcCd, String vanIsttCd, String ornId, String ornPktNo, String ornPktDivCd, String ornJobDivCd, String trdDt, String trdTm, long trdAmt, String notiClssCd);
    DpmnNoti findByGlobalIdAndOutRsltCdAndOutStatCdAndSvcCdAndOrnIdAndOrnPktDivCdAndOrnJobDivCdAndMid(String globalId, String outRsltCd, String outStatCd, String svcCd, String ornId, String ornPktDivCd, String ornJobDivCd, String mid);

    //우리은행 모계좌 조회 TODO 계좌추가 or 수정되면 해당쿼리 조회해서 확인할것!
    @Query(value = "SELECT \n" +
            "  T.M_ID ,\n" +
            "  T.BANK_CD ,\n" +
            "  RMT.ACNT_NO_ENC  AS RMTM_ACNT_ENC\n" +
            "FROM BAS.TB_M_DW_MACNT T\n" +
            "LEFT OUTER JOIN BAS.TB_CPN_ACNT RMT\n" +
            "ON T.RMT_MACNT_ID = RMT.ACNT_ID\n" +
            "AND RMT.USE_YN    = 'Y'\n" +
            "WHERE 1    = 1\n" +
            "AND T.M_ID = :mid \n" +
            "AND SYSDATE BETWEEN t.ST_DATE AND t.ED_DATE\n" +
            "and T.USE_YN = 'Y'\n" +
            "AND ROWNUM <=1 ", nativeQuery = true)
    Object findByMAcntEnc(@Param("mid")String mid);
}
