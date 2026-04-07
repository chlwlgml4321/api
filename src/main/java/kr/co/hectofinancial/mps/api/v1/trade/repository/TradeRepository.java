package kr.co.hectofinancial.mps.api.v1.trade.repository;

import kr.co.hectofinancial.mps.api.v1.trade.domain.Trade;
import kr.co.hectofinancial.mps.api.v1.trade.domain.TradePK;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Pay.Pay;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.PayCancel.PayCancel;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Use.Use;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseCancel.UseCancel;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.UseEach.UseEach;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.Withdrawal.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, TradePK>, JpaSpecificationExecutor<Trade>, TradeRepositoryCustom, Pay, PayCancel, Use, UseCancel, Withdrawal, UseEach {

    Optional<Trade> findTradeByMpsCustNoAndTrdNoAndTrdDt(String mpsCustNo, String trdNo, String trdDt);
    long countByMpsCustNoAndTrdDivCdAndTrdDtBetween(String custNo, String divCd, String stDate, String edDate);
    Trade findTopByMpsCustNoAndMidAndTrdDivCdInOrderByCreatedDateDesc(String custNo, String mid, List<String> trdDivCdList);
    List<Trade> findByMpsCustNoAndTrdDivCdAndTrdDtAndCsrcIssReqYnAndCsrcIssStatCdNotIn(String custNo, String divCd, String tgtDt, String csrcIssReqYn, List<String> csrcIssStatCd);

    List<Trade> findByMpsCustNoAndTrdDivCdIn(String custNo, List<String> trdDivCdList);
    Trade findByTrdNoAndTrdDt(String trdNo, String trdDt);

    @Query(value = "SELECT *\n" +
            "from MPS.PM_MPS_TRD\n" +
            "WHERE 1=1\n" +
            "AND TRD_DIV_CD = :trdDivCd \n" +
            "AND MPS_CUST_NO = :mpsCustNo \n" +
            "AND M_TRD_NO = :mTrdNo " +
            "AND STOR_CD = :ornId " +
            "AND CARD_MNG_NO = :cardMngNo ", nativeQuery = true)
    List<Trade> findByTrdDivCdAndMTrdNoAndStorCdAndMpsCustNo(@Param("trdDivCd") String trdDivCd, @Param("mTrdNo") String mTrdNo, @Param("ornId") String ornId, @Param("mpsCustNo") String mpsCustNo, @Param("cardMngNo") String cardMngNo); //비씨 승인취소시 원거래조회 쿼리(승인일자X)

    @Query(value = "SELECT t FROM Trade t WHERE t.mpsCustNo = :mpsCustNo AND t.trdDt BETWEEN :stDate AND :edDate AND t.mTrdNo = :mTrdNo AND rownum <= 5")
    List<Trade> findTradesByMpsCustNoAndPeriodAndMTrdNo(@Param("mpsCustNo") String mpsCustNo, @Param("stDate") String stDate, @Param("edDate") String edDate, @Param("mTrdNo") String mTrdNo);
    @Modifying
    @Query(value = "UPDATE MPS.PM_MPS_TRD \n" +
            "SET CHRG_TRD_NO = :chrgTrdNo \n" +
            "WHERE TRD_NO = :trdNo \n" +
            "AND TRD_DT = :trdDt ", nativeQuery = true)
    int updateChrgTrdNoAndRmkByTrdNoAndTrdDtAndCustNo(@Param("chrgTrdNo") String chrgTrdNo, @Param("trdNo") String trdNo, @Param("trdDt") String trdDt);

}
