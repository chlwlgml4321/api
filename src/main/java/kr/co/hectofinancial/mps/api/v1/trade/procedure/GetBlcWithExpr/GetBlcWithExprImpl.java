package kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlcWithExpr;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

@Slf4j
public class GetBlcWithExprImpl implements GetBlcWithExpr {

    private final String GET_BLC_WITH_EXPR = " MPS.PKG_MPS_CUST_WLLT_V2.GET_BLC_WITH_EXPR";
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public GetBlcWithExprOut blcWithExpr(GetBlcWithExprIn getBlcWithExprIn) {

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(GET_BLC_WITH_EXPR);

        query.registerStoredProcedureParameter("inMpsCustNo", String.class, ParameterMode.IN);//inMpsCustNo
        query.registerStoredProcedureParameter("inChrgLmtAmt", Long.class, ParameterMode.IN);//inChrgLmtAmt
        query.registerStoredProcedureParameter("inWorkerID", String.class, ParameterMode.IN);//inWorkerID
        query.registerStoredProcedureParameter("inWorkerIP", String.class, ParameterMode.IN);//inWorkerIP

        query.registerStoredProcedureParameter("outResCd", Long.class, ParameterMode.OUT);//outResCd
        query.registerStoredProcedureParameter("outResMsg", String.class, ParameterMode.OUT);//outResMsg
        query.registerStoredProcedureParameter("outMnyBlc", Long.class, ParameterMode.OUT);//outMnyBlc
        query.registerStoredProcedureParameter("outPntBlc", Long.class, ParameterMode.OUT);//outPntBlc
        query.registerStoredProcedureParameter("outWaitMnyBlc", Long.class, ParameterMode.OUT);//outWaitMnyBlc
        query.registerStoredProcedureParameter("outChrgPsbAmt", Long.class, ParameterMode.OUT);//outWaitMnyBlc
        query.registerStoredProcedureParameter("outExprPntAmt", Long.class, ParameterMode.OUT);//outExprPntAmt
        query.registerStoredProcedureParameter("outUseTrdNo", String.class, ParameterMode.OUT);//outUseTrdNo
        query.registerStoredProcedureParameter("outUseTrdDt", String.class, ParameterMode.OUT);//outUseTrdDt
        query.registerStoredProcedureParameter("outUseTrdTm", String.class, ParameterMode.OUT);//outUseTrdTm

        query.setParameter("inMpsCustNo", getBlcWithExprIn.getInMpsCustNo());
        query.setParameter("inChrgLmtAmt", getBlcWithExprIn.getInChrgLmtAmt());
        query.setParameter("inWorkerID", getBlcWithExprIn.getInWorkerId());
        query.setParameter("inWorkerIP", getBlcWithExprIn.getInWorkerIp());

        query.execute();

        return GetBlcWithExprOut.builder()
                .outResCd((Number) query.getOutputParameterValue("outResCd"))
                .outResMsg((String) query.getOutputParameterValue("outResMsg"))
                .outMnyBlc((Number) query.getOutputParameterValue("outMnyBlc"))
                .outPntBlc((Number) query.getOutputParameterValue("outPntBlc"))
                .outWaitMnyBlc((Number) query.getOutputParameterValue("outWaitMnyBlc"))
                .outChrgPsbAmt((Number) query.getOutputParameterValue("outChrgPsbAmt"))
                .outExprPntAmt((Number) query.getOutputParameterValue("outExprPntAmt"))
                .outUseTrdNo((String) query.getOutputParameterValue("outUseTrdNo"))
                .outUseTrdDt((String) query.getOutputParameterValue("outUseTrdDt"))
                .outUseTrdTm((String) query.getOutputParameterValue("outUseTrdTm"))
                .build();
    }
}
