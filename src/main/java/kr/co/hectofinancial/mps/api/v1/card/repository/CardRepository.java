package kr.co.hectofinancial.mps.api.v1.card.repository;

import kr.co.hectofinancial.mps.api.v1.card.domain.BpcCust;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CardRepository extends JpaRepository<BpcCust, String>, JpaSpecificationExecutor<BpcCust> {


    BpcCust findByOrnIdAndCardStatCdNotInAndCardNoEncAndLastCardDivCdNotIn(String ornId, List<String> cardStatCd, String cardNoEnc, List<String> lastCardDivCd); //승인카드조회

    BpcCust findByOrnIdAndCardNoEnc(String ornId, String cardNoEnc); //승인취소카드조회
    BpcCust findByCardMngNoAndCardStatCdNotInAndLastCardDivCdNotIn(String cardMngNo, List<String> cardStatCd, List<String> lastCardDivCd);

    BpcCust findByCardMngNo(String cardMngNo);

}
