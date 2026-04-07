package kr.co.hectofinancial.mps.api.v1.trade.procedure.Pay;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

public class PayImpl implements Pay {

    private final String DO_PAY = " MPS.PKG_MPS_CUST_WLLT_V2.PAY";
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PayOut doPay(PayIn payIn) {

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(DO_PAY);

        query.registerStoredProcedureParameter("inMpsCustNo", String.class, ParameterMode.IN);//inMpsCustNo
        query.registerStoredProcedureParameter("inChrgMeanCd", String.class, ParameterMode.IN);//inChrgMeanCd
        query.registerStoredProcedureParameter("inTrdDivCd", String.class, ParameterMode.IN);//inTrdDivCd
        query.registerStoredProcedureParameter("inCustDivCd", String.class, ParameterMode.IN);//inCustDivCd
        query.registerStoredProcedureParameter("inTrdDivDtlCd", String.class, ParameterMode.IN);//inTrdDivDtlCd
        query.registerStoredProcedureParameter("inChrgLmtAmt", Long.class, ParameterMode.IN);//inChrgLmtAmt
        query.registerStoredProcedureParameter("inMID", String.class, ParameterMode.IN);//inMID
        query.registerStoredProcedureParameter("inPayTrdNo", String.class, ParameterMode.IN);//inPayTrdNo
        query.registerStoredProcedureParameter("inPayTrdDt", String.class, ParameterMode.IN);//inPayTrdDt
        query.registerStoredProcedureParameter("inTrdAmt", Long.class, ParameterMode.IN);//inTrdAmt
        query.registerStoredProcedureParameter("inBlc", Long.class, ParameterMode.IN);//inBlc
        query.registerStoredProcedureParameter("inVldPd", String.class, ParameterMode.IN);//inVldPd
        query.registerStoredProcedureParameter("inPntId", String.class, ParameterMode.IN);//inPntId
        query.registerStoredProcedureParameter("inPayRsn", String.class, ParameterMode.IN);//inPayRsn
        query.registerStoredProcedureParameter("inWorkerID", String.class, ParameterMode.IN);//inWorkerID
        query.registerStoredProcedureParameter("inWorkerIP", String.class, ParameterMode.IN);//inWorkerIP

        query.registerStoredProcedureParameter("outResCd", Long.class, ParameterMode.OUT);//outResCd
        query.registerStoredProcedureParameter("outResMsg", String.class, ParameterMode.OUT);//outResMsg
        query.registerStoredProcedureParameter("outAmt", Long.class, ParameterMode.OUT);//outAmt
        query.registerStoredProcedureParameter("outMnyBlc", Long.class, ParameterMode.OUT);//outMnyBlc
        query.registerStoredProcedureParameter("outPntBlc", Long.class, ParameterMode.OUT);//outPntBlc
        query.registerStoredProcedureParameter("outWaitMnyBlc", Long.class, ParameterMode.OUT);//outWaitMnyBlc

        query.setParameter("inMpsCustNo", payIn.getInMpsCustNo());
        query.setParameter("inChrgMeanCd", payIn.getInChrgMeanCd());
        query.setParameter("inTrdDivCd", payIn.getInTrdDivCd());
        query.setParameter("inTrdDivDtlCd", payIn.getInTrdDivDtlCd());
        query.setParameter("inCustDivCd", payIn.getInCustDivCd());
        query.setParameter("inChrgLmtAmt", payIn.getInChrgLmtAmt());
        query.setParameter("inMID", payIn.getInMID());
        query.setParameter("inPayTrdNo", payIn.getInPayTrdNo());
        query.setParameter("inPayTrdDt", payIn.getInPayTrdDt());
        query.setParameter("inTrdAmt", payIn.getInTrdAmt());
        query.setParameter("inBlc", payIn.getInBlc());
        query.setParameter("inVldPd", payIn.getInVldPd());
        query.setParameter("inPntId", payIn.getInPntId());
        query.setParameter("inPayRsn", payIn.getInPayRsn());
        query.setParameter("inWorkerID", payIn.getInWorkerID());
        query.setParameter("inWorkerIP", payIn.getInWorkerIP());

        query.execute();

        return PayOut.builder()
                .outResCd((Number) query.getOutputParameterValue("outResCd"))
                .outResMsg((String) query.getOutputParameterValue("outResMsg"))
                .outAmt((Number) query.getOutputParameterValue("outAmt"))
                .outMnyBlc((Number) query.getOutputParameterValue("outMnyBlc"))
                .outPntBlc((Number) query.getOutputParameterValue("outPntBlc"))
                .outWaitMnyBlc((Number) query.getOutputParameterValue("outWaitMnyBlc"))
                .build();

    }
}
