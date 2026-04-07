package kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlc;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

@Slf4j
public class GetBlcImpl implements GetBlc {

    private final String GET_BLC = " MPS.PKG_MPS_CUST_WLLT_V2.GET_BLC";
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public GetBlcOut getBlc(GetBlcIn getBlcIn) {

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(GET_BLC);

        query.registerStoredProcedureParameter("inMpsCustNo", String.class, ParameterMode.IN);//inMpsCustNo
        query.registerStoredProcedureParameter("inChrgLmtAmt", Long.class, ParameterMode.IN);//inChrgLmtAmt

        query.registerStoredProcedureParameter("outResCd", Long.class, ParameterMode.OUT);//outResCd
        query.registerStoredProcedureParameter("outResMsg", String.class, ParameterMode.OUT);//outResMsg
        query.registerStoredProcedureParameter("outMnyBlc", Long.class, ParameterMode.OUT);//outMnyBlc
        query.registerStoredProcedureParameter("outPntBlc", Long.class, ParameterMode.OUT);//outPntBlc
        query.registerStoredProcedureParameter("outWaitMnyBlc", Long.class, ParameterMode.OUT);//outWaitMnyBlc
        query.registerStoredProcedureParameter("outChrgPsbAmt", Long.class, ParameterMode.OUT);//outWaitMnyBlc

        query.setParameter("inMpsCustNo", getBlcIn.getInMpsCustNo());
        query.setParameter("inChrgLmtAmt", getBlcIn.getInChrgLmtAmt());

        query.execute();

        return GetBlcOut.builder()
                .outResCd((Number) query.getOutputParameterValue("outResCd"))
                .outResMsg((String) query.getOutputParameterValue("outResMsg"))
                .outMnyBlc((Number) query.getOutputParameterValue("outMnyBlc"))
                .outPntBlc((Number) query.getOutputParameterValue("outPntBlc"))
                .outWaitMnyBlc((Number) query.getOutputParameterValue("outWaitMnyBlc"))
                .outChrgPsbAmt((Number) query.getOutputParameterValue("outChrgPsbAmt"))
                .build();
    }
}
