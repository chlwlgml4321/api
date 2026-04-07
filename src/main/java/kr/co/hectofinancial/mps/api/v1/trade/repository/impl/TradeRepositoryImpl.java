package kr.co.hectofinancial.mps.api.v1.trade.repository.impl;

import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.dto.TradeUseSummaryResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.repository.TradeRepositoryCustom;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TradeRepositoryImpl implements TradeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Trade> findAllByMpsCustNoAndTrdDtLikeAndTrdDivCdIn(Map<String,Object> param, Pageable pageable) {
        String mpsCustNo = String.valueOf(param.get("mpsCustNo"));
        String trdDt = String.valueOf(param.get("trdDt"));
        List<String> trdDivCds = (List<String>) param.get("trdDivCds");
        String mTrdNo = String.valueOf(param.get("mTrdNo"));
        String trdNo = String.valueOf(param.get("trdNo"));
        String blcDivCd = String.valueOf(param.get("blcDivCd"));
        String cardTrdOnlyYn = String.valueOf(param.get("cardTrdOnlyYn"));

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Trade> cq = cb.createQuery(Trade.class);
        Root<Trade> trade = cq.from(Trade.class);

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isBlank(CommonUtil.nullTrim(trdDt))) {
            trdDt = new SimpleDateFormat("yyyyMM").format(Calendar.getInstance().getTime());
        }
        String yyyy = trdDt.substring(0, 4);
        String mm = trdDt.substring(4, 6);

        String stDate = yyyy + mm + "01";
        String edDate = DateTimeUtil.calculateEndDate(trdDt);

        /*조회조건*/
        predicates.add(cb.equal(trade.get("mpsCustNo"), mpsCustNo));
        predicates.add(cb.greaterThanOrEqualTo(trade.get("trdDt"), stDate));
        predicates.add(cb.lessThanOrEqualTo(trade.get("trdDt"), edDate));

        if (trdDivCds.size() > 0) {
            predicates.add(trade.get("trdDivCd").in(trdDivCds));
        }
        if (StringUtils.isNotBlank(CommonUtil.nullTrim(trdNo))) {
            predicates.add(cb.equal(trade.get("trdNo"), trdNo));
        }
        if (StringUtils.isNotBlank(CommonUtil.nullTrim(mTrdNo))) {
            predicates.add(cb.equal(trade.get("mTrdNo"), mTrdNo));
        }
        if (StringUtils.isNotBlank(CommonUtil.nullTrim(blcDivCd))) {
            if ("M".equals(blcDivCd)) {
                //머니, 대기머니
                predicates.add(cb.or(cb.greaterThan(trade.get("mnyAmt"), 0L), cb.greaterThan(trade.get("waitMnyAmt"), 0L)));
            } else if ("P".equals(blcDivCd)) {
                //포인트 
                predicates.add(cb.greaterThan(trade.get("pntAmt"), 0L));
            }
        }
        if ("Y".equals(cardTrdOnlyYn)) {
            predicates.add(cb.isNotNull(trade.get("cardMngNo")));
        }
        cq.orderBy(cb.desc(trade.get("mpsCustNo")), cb.desc(trade.get("trdNo")),cb.desc(trade.get("trdDt")), cb.desc(trade.get("trdTm")));
        cq.where(predicates.toArray(new Predicate[0]));

        // Execute query
        List<Trade> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        return resultList;

//        if (resultList.isEmpty()) {
//            return new PageImpl<>(new ArrayList<>(), pageable, 0);
//        }

//        // Count query
//        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
//        Root<Trade> tradeCountRoot = countQuery.from(Trade.class);
//        countQuery.select(cb.count(tradeCountRoot))
//                .where(predicates.toArray(new Predicate[0]));
//        Long count = entityManager.createQuery(countQuery).getSingleResult();

//        return new PageImpl<>(resultList, pageable, 0);
    }

    @Override
    public TradeUseSummaryResponseDto sumTradeUseByMpsCustNoAndTrdDtLike(Map<String, Object> param) {
        String mpsCustNo = String.valueOf(param.get("mpsCustNo"));
        String trdDt = String.valueOf(param.get("trdDt"));
        String cardTrdOnlyYn = String.valueOf(param.get("cardTrdOnlyYn"));

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Trade> trade = cq.from(Trade.class);

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isBlank(CommonUtil.nullTrim(trdDt))) {
            trdDt = new SimpleDateFormat("yyyyMM").format(Calendar.getInstance().getTime());
        }
        String yyyy = trdDt.substring(0, 4);
        String mm = trdDt.substring(4, 6);

        String stDate = yyyy + mm + "01";
        String edDate = DateTimeUtil.calculateEndDate(trdDt);

        /*조회조건*/
        predicates.add(cb.equal(trade.get("mpsCustNo"), mpsCustNo));
        predicates.add(cb.greaterThanOrEqualTo(trade.get("trdDt"), stDate));
        predicates.add(cb.lessThanOrEqualTo(trade.get("trdDt"), edDate));
        predicates.add(cb.equal(trade.get("trdDivCd"), TrdDivCd.COMMON_USE.getTrdDivCd())); //사용 고정

        if ("Y".equalsIgnoreCase(cardTrdOnlyYn)) {
            predicates.add(cb.isNotNull(trade.get("cardMngNo")));
        }

        Expression<Long> trdNet = cb.diff(trade.get("trdAmt"), trade.get("cnclTrdAmt"));//거래금액 - 취소거래금액
        Expression<Long> mnyNet = cb.diff(trade.get("mnyAmt"), trade.get("cnclMnyAmt"));//머니금액 - 취소머니금액
        Expression<Long> pntNet = cb.diff(trade.get("pntAmt"), trade.get("cnclPntAmt"));//포인트금액 - 취소포인트금액
        Expression<Long> trdCnt = cb.count(trade);

        cq.multiselect(
                cb.coalesce(cb.sum(trdNet), 0L).alias("sumTrdAmt"),
                cb.coalesce(cb.sum(mnyNet), 0L).alias("sumMnyAmt"),
                cb.coalesce(cb.sum(pntNet), 0L).alias("sumPntAmt"),
                cb.coalesce(trdCnt, 0L).alias("trdCnt")
        );

        cq.where(predicates.toArray(new Predicate[0]));

        Tuple tuple = entityManager.createQuery(cq).getSingleResult();

        return TradeUseSummaryResponseDto.builder()
                .custNo(mpsCustNo)
                .period(trdDt)
                .trdCnt(String.valueOf(tuple.get("trdCnt")))
                .trdAmt(String.valueOf(tuple.get("sumTrdAmt")))
                .mnyAmt(String.valueOf(tuple.get("sumMnyAmt")))
                .pntAmt(String.valueOf(tuple.get("sumPntAmt")))
                .build();
    }
}
