package kr.co.hectofinancial.mps.api.v1.trade.procedure.PntExpr;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

@Slf4j
public class PntExprImpl implements PntExpr {

    private final String CNCL_PNT_EXPR = " MPS.PKG_MPS_CUST_WLLT_V2.CNCL_PNT_EXPR";
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PntExprOut pointExpr(PntExprIn pntExprIn) {

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(CNCL_PNT_EXPR);

        query.registerStoredProcedureParameter("inMpsCustNo", String.class, ParameterMode.IN);//inMpsCustNo
        query.registerStoredProcedureParameter("inTrdDivCd", String.class, ParameterMode.IN);//inTrdDivCd
        query.registerStoredProcedureParameter("inUseTrdNo", String.class, ParameterMode.IN);//inUseTrdNo
        query.registerStoredProcedureParameter("inUseTrdDt", String.class, ParameterMode.IN);//inUseTrdDt
        query.registerStoredProcedureParameter("inCnclTrdNo", String.class, ParameterMode.IN);//inCnclTrdNo
        query.registerStoredProcedureParameter("inCnclTrdDt", String.class, ParameterMode.IN);//inCnclTrdDt
        query.registerStoredProcedureParameter("inTrdAmt", Long.class, ParameterMode.IN);//inTrdAmt
        query.registerStoredProcedureParameter("inWorkerID", String.class, ParameterMode.IN);//inWorkerID
        query.registerStoredProcedureParameter("inWorkerIP", String.class, ParameterMode.IN);//inWorkerIP

        query.registerStoredProcedureParameter("outResCd", Long.class, ParameterMode.OUT);//outResCd
        query.registerStoredProcedureParameter("outResMsg", String.class, ParameterMode.OUT);//outResMsg
        query.registerStoredProcedureParameter("outExprPntAmt", Long.class, ParameterMode.OUT);//outExprPntAmt
        query.registerStoredProcedureParameter("outMnyBlc", Long.class, ParameterMode.OUT);//outMnyBlc
        query.registerStoredProcedureParameter("outPntBlc", Long.class, ParameterMode.OUT);//outPntBlc
        query.registerStoredProcedureParameter("outWaitMnyBlc", Long.class, ParameterMode.OUT);//outWaitMnyBlc

        query.setParameter("inMpsCustNo", pntExprIn.getInMpsCustNo());
        query.setParameter("inTrdDivCd", pntExprIn.getInTrdDivCd());
        query.setParameter("inUseTrdNo", pntExprIn.getInUseTrdNo());
        query.setParameter("inUseTrdDt", pntExprIn.getInUseTrdDt());
        query.setParameter("inCnclTrdNo", pntExprIn.getInCnclTrdNo());
        query.setParameter("inCnclTrdDt", pntExprIn.getInCnclTrdDt());
        query.setParameter("inTrdAmt", pntExprIn.getInTrdAmt());
        query.setParameter("inWorkerID", pntExprIn.getInWorkerID());
        query.setParameter("inWorkerIP", pntExprIn.getInWorkerIP());

        query.execute();

        return PntExprOut.builder()
                .outResCd((Number) query.getOutputParameterValue("outResCd"))
                .outResMsg((String) query.getOutputParameterValue("outResMsg"))
                .outMnyBlc((Number) query.getOutputParameterValue("outMnyBlc"))
                .outPntBlc((Number) query.getOutputParameterValue("outPntBlc"))
                .outExprPntAmt((Number) query.getOutputParameterValue("outExprPntAmt"))
                .outWaitMnyBlc((Number) query.getOutputParameterValue("outWaitMnyBlc"))
                .build();
    }
}
