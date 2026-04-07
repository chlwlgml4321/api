package kr.co.hectofinancial.mps.api.v1.common.service;

import kr.co.hectofinancial.mps.global.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * 시퀀스 생성 service
 */
@Service
@RequiredArgsConstructor
public class SequenceService {

    private final EntityManager entityManager;

    /**
     * @return SEQ_PM_MPS_TRD_01 거래번호 (거래 / 거래실패 테이블)
     */
    @Transactional
    public String generateTradeSeq01() {
        String sql = "SELECT MPS.SEQ_PM_MPS_TRD_01.nextVal FROM dual";
        Long seqVal = ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
        String dateString = CommonUtil.getDateTimeStr();
        return dateString + String.format("%08d", seqVal);
    }

    /**
     * @return SEQ_PM_MPS_PAY_MNY_01 지급 머니 원장 번호
     */
    @Transactional
    public String generatePayMoneySeq01() {
        String sql = "SELECT MPS.SEQ_PM_MPS_PAY_MNY_01.nextVal FROM dual";
        Long seqVal = ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
        String dateString = CommonUtil.getDateTimeStr();
        return dateString + String.format("%08d", seqVal);
    }

    /**
     * @return SEQ_PM_MPS_PAY_PNT_01 지급 포인트 원장 번호
     */
    @Transactional
    public String generatePayPointSeq01() {
        String sql = "SELECT MPS.SEQ_PM_MPS_PAY_PNT_01.nextVal FROM dual";
        Long seqVal = ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
        String dateString = CommonUtil.getDateTimeStr();
        return dateString + String.format("%08d", seqVal);
    }

    /**
     * @return TRD.SEQ_TB_RFD_RCPT_01 환불 원장 번호
     */
    @Transactional
    public String generateRfdRcptSeq01(){
        String sql = "SELECT 'RF'||TO_CHAR(SYSDATE,'YYMMDD')||LPAD(TRD.SEQ_TB_RFD_RCPT_01.NEXTVAL,8,'0') FROM DUAL";
        Object seqVal = entityManager.createNativeQuery(sql).getSingleResult();
        return String.format("%s", seqVal);
    }

    /**
     * 
     * @return TRD.SEQ_PM_EZP_CSRC_ISS_01 현금영수증 접수 원장 번호
     */
    @Transactional
    public String generateCsrcTradeSeq01(){
        String sql = "SELECT TO_CHAR(SYSDATE, 'YYYYMMDD')||SUBSTR('0000000000'||TRD.SEQ_PM_EZP_CSRC_ISS_01.NEXTVAL, -10) FROM DUAL";
        Object seqVal = entityManager.createNativeQuery(sql).getSingleResult();
        return String.format("%s", seqVal);
    }

    @Transactional
    public String generatePyNtcSendSeq01(){
        String sql = "SELECT TO_CHAR(SYSDATE,'YYYYMMDD')||LPAD(TRD.SEQ_PY_NTC_SEND_01.NEXTVAL,8,'0') FROM DUAL";
        Object seqVal = entityManager.createNativeQuery(sql).getSingleResult();
        return String.format("%s", seqVal);
    }

    @Transactional
    public String generateBpcApprReqSeq01(){
        String sql = "SELECT MPS.SEQ_PM_BPC_APPR_REQ_01.nextVal FROM dual";
        Long seqVal = ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
        String dateString = CommonUtil.getDateTimeStr();
        return dateString + String.format("%08d", seqVal);
    }

    @Transactional
    public String generateGiftCardNo(String mId) {
        String sql = "SELECT MPS.FN_GEN_MPS_PIN('" + mId + "') FROM DUAL";
        return (String) entityManager.createNativeQuery(sql).getSingleResult();
    }

    @Transactional
    public List generateGiftCardNoList(String mId, int size) {
        String sql = "SELECT MPS.FN_GEN_MPS_PIN('" + mId + "') FROM DUAL CONNECT BY LEVEL <= " + size;
        return entityManager.createNativeQuery(sql).setMaxResults(size).getResultList();
    }

    @Transactional
    public String generateMpsNotiSendSeq01(){
        String sql = "SELECT MPS.SEQ_TB_MPS_NOTI_SEND_01.NEXTVAL FROM DUAL";
        Long seqVal = ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
        String dateString = CommonUtil.getDateStr();
        return dateString + String.format("%08d", seqVal);
    }

    @Transactional
    public String generateDistributorTradeSeq01() {
        String sql = "SELECT MPS.SEQ_TB_MPS_NOTI_SEND_01.nextVal FROM dual";
        Long seqVal = ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
        String dateString = CommonUtil.getDateStr();
        return dateString + String.format("%08d", seqVal);
    }
}
