package kr.co.hectofinancial.mps.api.v1.trade.procedure.GetMWAmt;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

@Slf4j
public class GetMWAmtImpl implements GetMWAmt {

    private final String GET_MW_AMT = " MPS.PKG_MPS_CUST_WLLT_V2.GET_MW_AMT";
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public GetMWAmtOut mwAmt(GetMWAmtIn getMWAmtIn) {

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(GET_MW_AMT);

        query.registerStoredProcedureParameter("inMpsCustNo", String.class, ParameterMode.IN);//inMpsCustNo

        query.registerStoredProcedureParameter("outResCd", Long.class, ParameterMode.OUT);//outResCd
        query.registerStoredProcedureParameter("outResMsg", String.class, ParameterMode.OUT);//outResMsg
        query.registerStoredProcedureParameter("outMwAmt", Long.class, ParameterMode.OUT);//outMwAmt

        query.setParameter("inMpsCustNo", getMWAmtIn.getInMpsCustNo());

        query.execute();

        return GetMWAmtOut.builder()
                .outResCd((Number) query.getOutputParameterValue("outResCd"))
                .outResMsg((String) query.getOutputParameterValue("outResMsg"))
                .outMwAmt((Number) query.getOutputParameterValue("outMwAmt"))
                .build();
    }
}
