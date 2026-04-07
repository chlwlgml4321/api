package kr.co.hectofinancial.mps.api.v1.trade.procedure.Use;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.Arrays;

public class UseImpl implements Use {

    private final String USE = " MPS.PKG_MPS_CUST_WLLT_V2.USE";
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UseOut use(UseIn params) {

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(USE);

        //In 변수
        Arrays.stream(params.getClass().getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);
                    query.registerStoredProcedureParameter(field.getName(), field.getType(), ParameterMode.IN);
                });


        //Out 변수
        Arrays.stream(UseOut.class.getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);
                    query.registerStoredProcedureParameter(field.getName(), field.getType(), ParameterMode.OUT);
                });

        query.setParameter("inMpsCustNo", params.getInMpsCustNo());
        query.setParameter("inTrdDivCd", params.getInTrdDivCd());
        query.setParameter("inBlcUseOrd", params.getInBlcUseOrd());
        query.setParameter("inUseTrdNo", params.getInUseTrdNo());
        query.setParameter("inUseTrdDt", params.getInUseTrdDt());
        query.setParameter("inTrdAmt", (Number) params.getInTrdAmt().longValue());
        query.setParameter("inBlc", (Number) params.getInBlc().longValue());
        query.setParameter("inWorkerID", params.getInWorkerID());
        query.setParameter("inWorkerIP", params.getInWorkerIP());

        query.execute();


        return UseOut.builder()
                .outResCd((Long) query.getOutputParameterValue("outResCd"))
                .outResMsg((String) query.getOutputParameterValue("outResMsg"))
                .outMnyAmt((Long) query.getOutputParameterValue("outMnyAmt"))
                .outPntAmt((Long) query.getOutputParameterValue("outPntAmt"))
                .outMnyBlc((Long) query.getOutputParameterValue("outMnyBlc"))
                .outPntBlc((Long) query.getOutputParameterValue("outPntBlc"))
                .outWaitMnyBlc((Long) query.getOutputParameterValue("outWaitMnyBlc"))
                .build();

    }
}
