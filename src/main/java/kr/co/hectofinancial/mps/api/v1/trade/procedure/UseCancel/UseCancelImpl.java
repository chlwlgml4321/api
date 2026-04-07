package kr.co.hectofinancial.mps.api.v1.trade.procedure.UseCancel;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.Arrays;

public class UseCancelImpl implements UseCancel {

    private final String USE_CANCEL = " MPS.PKG_MPS_CUST_WLLT_V2.USE_CNCL";
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UseCancelOut useCancel(UseCancelIn params) {

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(USE_CANCEL);

        //In 변수
        Arrays.stream(params.getClass().getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);
                    query.registerStoredProcedureParameter(field.getName(), field.getType(), ParameterMode.IN);
                });

        //Out 변수
        Arrays.stream(UseCancelOut.class.getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);
                    query.registerStoredProcedureParameter(field.getName(), field.getType(), ParameterMode.OUT);
                });

        query.setParameter("inMpsCustNo", params.getInMpsCustNo());
        query.setParameter("inTrdDivCd", params.getInTrdDivCd());
        query.setParameter("inCustDivCd", params.getInCustDivCd());
        query.setParameter("inChrgLmtAmt", params.getInChrgLmtAmt());
        query.setParameter("inUseTrdNo", params.getInUseTrdNo());
        query.setParameter("inUseTrdDt", params.getInUseTrdDt());
        query.setParameter("inCnclTrdNo", params.getInCnclTrdNo());
        query.setParameter("inCnclTrdDt", params.getInCnclTrdDt());
        query.setParameter("inMnyAmt", params.getInMnyAmt());
        query.setParameter("inPntAmt", params.getInPntAmt());
        query.setParameter("inBlc", params.getInBlc());
        query.setParameter("inMID", params.getInMID());
        query.setParameter("inWorkerID", params.getInWorkerID());
        query.setParameter("inWorkerIP", params.getInWorkerIP());

        query.execute();

        return UseCancelOut.builder()
                .outResCd((Long) query.getOutputParameterValue("outResCd"))
                .outResMsg((String) query.getOutputParameterValue("outResMsg"))
                .outMnyAmt((Long) query.getOutputParameterValue("outMnyAmt"))
                .outPntAmt((Long) query.getOutputParameterValue("outPntAmt"))
                .outExprPntAmt((Long) query.getOutputParameterValue("outExprPntAmt"))
                .outWaitMnyAmt((Long) query.getOutputParameterValue("outWaitMnyAmt"))
                .outMnyBlc((Long) query.getOutputParameterValue("outMnyBlc"))
                .outPntBlc((Long) query.getOutputParameterValue("outPntBlc"))
                .outWaitMnyBlc((Long) query.getOutputParameterValue("outWaitMnyBlc"))
                .build();

    }
}
