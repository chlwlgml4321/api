package kr.co.hectofinancial.mps.test.feign;

import kr.co.hectofinancial.mps.api.v1.card.dto.CardUseApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.*;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.*;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletBalanceRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletCancelRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletUseEachRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletUseRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "mpsClientPrd", url = "${feign.client.setting.url-prd:defaultValue}", configuration = FeignClientConfig.class)
public interface MpsClientPrd {
    ///////////////////////지갑
    /**
     * 사용
     *
     * @param walletUseRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.wallet.use:defaultValue}")
    ResponseEntity<Object> useWallet(@RequestBody WalletUseRequestDto walletUseRequestDto);

    /**
     * 사용 취소
     *
     * @param walletCancelRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.wallet.useCancel:defaultValue}")
    ResponseEntity<Object> cancelUseWallet(@RequestBody WalletCancelRequestDto walletCancelRequestDto);

    /**
     * 각각 사용
     * @param walletUseEachRequestDto
     * @return
     */
    @PostMapping("/wallet/use/each")
    ResponseEntity<Object> useWalletEach(@RequestBody WalletUseEachRequestDto walletUseEachRequestDto);
    /**
     * 잔액 조회
     *
     * @param walletBalanceRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.wallet.balance:defaultValue}")
    ResponseEntity<Object> getWalletBalance(@RequestBody WalletBalanceRequestDto walletBalanceRequestDto);

    /**
     * 잔액 조회 상세
     *
     * @param walletBalanceRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.wallet.balanceDetail:defaultValue}")
    ResponseEntity<Object> getWalletBalanceDetail(@RequestBody WalletBalanceRequestDto walletBalanceRequestDto);

    /**
     * 출금 가능 잔액 조회
     *
     * @param withdrawalMoneyRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.wallet.withdrawal:defaultValue}")
    ResponseEntity<Object> getWithdrawableWalletBalance(@RequestBody WithdrawalMoneyRequestDto withdrawalMoneyRequestDto);

    //////////////////////////////////지급

    /**
     * 지급
     *
     * @param chargeApprovalRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.approval.charge:defaultValue}")
    ResponseEntity<Object> approvalCharge(@RequestBody ChargeApprovalRequestDto chargeApprovalRequestDto);

    /**
     * 지급 취소
     *
     * @param chargeApprovalCancelRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.approval.chargeCancel:defaultValue}")
    ResponseEntity<Object> approvalChargeCancel(@RequestBody ChargeApprovalCancelRequestDto chargeApprovalCancelRequestDto);

    //////////////////////////////////머니

    /**
     * 머니 출금
     *
     * @param withdrawApprovalRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.money.withdrawal:defaultValue}")
    ResponseEntity<Object> withdrawMoney(@RequestBody WithdrawApprovalRequestDto withdrawApprovalRequestDto);

    /**
     * 대기머니 출금
     *
     * @param waitMnyWithdrawApprovalRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.money.waitMoneyWithdrawal:defaultValue}")
    ResponseEntity<Object> withdrawWaitMoney(@RequestBody WaitMnyWithdrawApprovalRequestDto waitMnyWithdrawApprovalRequestDto);

    /**
     * 머니 선물
     *
     * @param moneyGiftRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.money.gift:defaultValue}")
    ResponseEntity<Object> giftMoney(@RequestBody MoneyGiftRequestDto moneyGiftRequestDto);
    @PostMapping("${feign.client.setting.money.willMnyWdYn:defaultValue}")
    ResponseEntity<Object> getWillWithdrawalMoney(@RequestBody WillMnyWithdrawalYnRequestDto willMnyWithdrawalYnRequestDto);


    /////////////////////////////거래

    /**
     * 거래 내역 조회
     *
     * @param tradeInfoListRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.trade.list:defaultValue}")
    ResponseEntity<Object> getTradeList(@RequestBody TradeInfoListRequestDto tradeInfoListRequestDto);

    /**
     * 거래 단건 상세 조회
     *
     * @param tradeInfoRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.trade.detail:defaultValue}")
    ResponseEntity<Object> getTradeDetail(@RequestBody TradeInfoRequestDto tradeInfoRequestDto);

    /////////////////////////////충전수단

    /**
     * 상점의 충전 수단 조회
     *
     * @param param
     * @return
     */
    @PostMapping("${feign.client.setting.market.chargeList:defaultValue}")
    ResponseEntity<Object> getMarketChargeList(@RequestBody Map<String, Object> param);

    /**
     * 회원 정보 조회
     *
     * @param param
     * @return
     */
    @PostMapping("${feign.client.setting.customer.info:defaultValue}")
    ResponseEntity<Object> getCustomerInfo(@RequestBody CustomerRequestDto customerRequestDto);

    /**
     * 카드사용
     *
     * @param param
     * @return
     */
    @PostMapping("${feign.client.setting.card.use:defaultValue}")
    ResponseEntity<Object> cardUse(@RequestBody CardUseApprovalRequestDto cardUseApprovalRequestDto);

    /**
     * 사용 거래 집계
     *
     * @param tradeUseSummaryRequestDto
     * @return
     */
    @PostMapping("${feign.client.setting.trade.use.sum:defaultValue}")
    ResponseEntity<Object> getTradeUseSum(@RequestBody TradeUseSummaryRequestDto tradeUseSummaryRequestDto);

}
