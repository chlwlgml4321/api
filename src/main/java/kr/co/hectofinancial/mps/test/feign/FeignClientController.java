//package kr.co.hectofinancial.mps.test.feign;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Profile;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
///**
// * LOCAL 환경에서 테스트 하는 것 처럼 TB 환경으로 테스트 할 수 있는 Controller
// * 직접 TB 로 API 테스트 할 경우, 일일히 파라미터 암호화 및 pktHash 생성 필요하나,
// * 해당 Controller 를 통한 테스트 시, LOCAL 환경처럼 암호화 혹은 pktHash 생성 불필요
// */
//@Profile("local")
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//public class FeignClientController {
//
//    private final FeignClientService feignClientService;
//
//
//    @PostMapping("/feign${feign.client.setting.wallet.use:defaultValue}/{profile}")
//    public ResponseEntity<Object> useWallet(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.useWallet(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.wallet.useCancel:defaultValue}/{profile}")
//    public ResponseEntity<Object> cancelWallet(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.cancelUseWallet(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.wallet.use:defaultValue}/each/{profile}")
//    public ResponseEntity<Object> useWalletEach(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.useWalletEach(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.wallet.balance:defaultValue}/{profile}")
//    public ResponseEntity<Object> getWalletBalance(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.getWalletBalance(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.wallet.balanceDetail:defaultValue}/{profile}")
//    public ResponseEntity<Object> getWalletBalanceDetail(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.getWalletBalanceDetail(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.wallet.withdrawal:defaultValue}/{profile}")
//    public ResponseEntity<Object> getWithdrawalMoneyAmt(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.getWithdrawableWalletBalance(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.approval.charge:defaultValue}/{profile}")
//    public ResponseEntity<Object> approvalCharge(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.approvalCharge(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.approval.chargeCancel:defaultValue}/{profile}")
//    public ResponseEntity<Object> approvalChargeCancel(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.approvalChargeCancel(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.money.withdrawal:defaultValue}/{profile}")
//    public ResponseEntity<Object> withdrawMoney(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.withdrawMoney(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.money.waitMoneyWithdrawal:defaultValue}/{profile}")
//    public ResponseEntity<Object> withdrawWaitMoney(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.withdrawWaitMoney(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.money.gift:defaultValue}/{profile}")
//    public ResponseEntity<Object> giftMoney(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.giftMoney(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.trade.list:defaultValue}/{profile}")
//    public ResponseEntity<Object> getTradeList(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.getTradeList(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.trade.detail:defaultValue}/{profile}")
//    public ResponseEntity<Object> getTradeDetail(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.getTradeDetail(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.market.chargeList:defaultValue}/{profile}")
//    public ResponseEntity<Object> getMarketChargeList(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.getMarketChargeList(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.money.willMnyWdYn:defaultValue}/{profile}")
//    public ResponseEntity<Object> getWillWithdrawalMoney(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.getWillWithdrawalMoney(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.customer.info:defaultValue}/{profile}")
//    public ResponseEntity<Object> getCustomerInfo(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//         return ResponseEntity.ok(feignClientService.getCustomerInfo(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.card.use:defaultValue}/{profile}")
//    public ResponseEntity<Object> cardUse(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.cardUse(feignClientRequestDto, profile));
//    }
//    @PostMapping("/feign${feign.client.setting.trade.use.sum:defaultValue}/{profile}")
//    public ResponseEntity<Object> getTradeUseSum(@PathVariable("profile") String profile, @RequestBody FeignClientRequestDto feignClientRequestDto) {
//        return ResponseEntity.ok(feignClientService.getTradeUseSum(feignClientRequestDto, profile));
//    }
//
//}
