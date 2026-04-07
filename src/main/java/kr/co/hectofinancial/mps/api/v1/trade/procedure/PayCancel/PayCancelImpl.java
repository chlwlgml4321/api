package kr.co.hectofinancial.mps.api.v1.trade.procedure.PayCancel;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

public class PayCancelImpl implements PayCancel {

    private final String DO_PAY_CANCEL = " MPS.PKG_MPS_CUST_WLLT_V2.PAY_CNCL";
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PayCancelOut doPayCancel(PayCancelIn doPayCancelIn) {

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(DO_PAY_CANCEL);

        query.registerStoredProcedureParameter("inMpsCustNo", String.class, ParameterMode.IN);//inMpsCustNo
        query.registerStoredProcedureParameter("inTrdDivCd", String.class, ParameterMode.IN);//inTrdDivCd
        query.registerStoredProcedureParameter("inPayTrdNo", String.class, ParameterMode.IN);//inPayTrdNo
        query.registerStoredProcedureParameter("inPayTrdDt", String.class, ParameterMode.IN);//inPayTrdDt
        query.registerStoredProcedureParameter("inUseTrdNo", String.class, ParameterMode.IN);//inUseTrNo
        query.registerStoredProcedureParameter("inUseTrdDt", String.class, ParameterMode.IN);//inUseTrDt
        query.registerStoredProcedureParameter("inTrdAmt", Long.class, ParameterMode.IN);//inTrdAmt
        query.registerStoredProcedureParameter("inBlc", Long.class, ParameterMode.IN);//inBlc
        query.registerStoredProcedureParameter("inWorkerID", String.class, ParameterMode.IN);//inWorkerID
        query.registerStoredProcedureParameter("inWorkerIP", String.class, ParameterMode.IN);//inWorkerIP

        query.registerStoredProcedureParameter("outResCd", Long.class, ParameterMode.OUT);//outResCd
        query.registerStoredProcedureParameter("outResMsg", String.class, ParameterMode.OUT);//outResMsg
        query.registerStoredProcedureParameter("outAmt", Long.class, ParameterMode.OUT);//outAmt
        query.registerStoredProcedureParameter("outMnyBlc", Long.class, ParameterMode.OUT);//outMnyBlc
        query.registerStoredProcedureParameter("outPntBlc", Long.class, ParameterMode.OUT);//outPntBlc
        query.registerStoredProcedureParameter("outWaitMnyBlc", Long.class, ParameterMode.OUT);//outWaitMnyBlc

        query.setParameter("inMpsCustNo", doPayCancelIn.getInMpsCustNo());
        query.setParameter("inTrdDivCd", doPayCancelIn.getInTrdDivCd());
        query.setParameter("inPayTrdNo", doPayCancelIn.getInPayTrdNo());
        query.setParameter("inPayTrdDt", doPayCancelIn.getInPayTrdDt());
        query.setParameter("inUseTrdNo", doPayCancelIn.getInUseTrNo());
        query.setParameter("inUseTrdDt", doPayCancelIn.getInUseTrDt());
        query.setParameter("inTrdAmt", doPayCancelIn.getInTrdAmt());
        query.setParameter("inBlc", doPayCancelIn.getInBlc());
        query.setParameter("inWorkerID", doPayCancelIn.getInWorkerID());
        query.setParameter("inWorkerIP", doPayCancelIn.getInWorkerIP());

        query.execute();

        return PayCancelOut.builder()
                .outResCd((Number) query.getOutputParameterValue("outResCd"))
                .outResMsg((String) query.getOutputParameterValue("outResMsg"))
                .outAmt((Number) query.getOutputParameterValue("outAmt"))
                .outMnyBlc((Number) query.getOutputParameterValue("outMnyBlc"))
                .outPntBlc((Number) query.getOutputParameterValue("outPntBlc"))
                .outWaitMnyBlc((Number) query.getOutputParameterValue("outWaitMnyBlc"))
                .build();

    }
}
