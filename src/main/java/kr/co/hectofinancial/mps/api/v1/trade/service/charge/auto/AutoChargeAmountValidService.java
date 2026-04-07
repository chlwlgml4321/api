package kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto;

import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.CustomerWalletResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.service.wallet.CustBalanceService;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoChargeAmountValidService {
    private final CustBalanceService custBalanceService;

    /**
     * 거래금액 0원 체크
     * @param trdAmt
     */
    public void checkTrdAmt(long trdAmt) {
        if (trdAmt == 0) {
            log.info("[checkTrdAmt FAIL] 거래금액 0원으로 검증 *실패!*");
            throw new RequestValidationException(ErrorCode.TRADE_AMT_ERROR);
        }
        log.info("[checkTrdAmt SUCCESS] 거래금액 0원이상으로 검증 통과");
    }

    /**
     * 포인트 잔액 검증
     * @param pntAmt
     * @param pntBlc
     */
    public void checkPntAmt(long pntAmt, long pntBlc) {
        if (pntAmt > pntBlc) {
            log.info("[checkPntAmt FAIL] 포인트 잔액 < 요청금액으로 검증 *실패!*");
            throw new RequestValidationException(ErrorCode.REQ_AMT_NOT_MATCHED);
        }
        log.info("[checkPntAmt SUCCESS] 포인트 잔액 >= 요청금액으로 검증 통과");
    }

    /**
     * 요청 파라미터 내 잔액과 실제잔액 체크
     * +
     * 거래금액이 지갑보유한도 + 포인트잔액 초과하는지 체크
     * @param custNo
     * @param trdAmt
     * @param mnyBlc
     * @param pntBlc
     * @param maxMnyBlc
     */
    public void checkRealBlc(String custNo, long trdAmt, long mnyBlc, long pntBlc, long maxMnyBlc) {
        CustomerWalletResponseDto custBalance = custBalanceService.getBalanceByCustomer(custNo, maxMnyBlc);
        if (mnyBlc != custBalance.getMnyBlc() || pntBlc != custBalance.getPntBlc()) {
            log.info("[checkRealBlc FAIL] 요청파라미터 잔액과 실제 잔액 *불일치!*");
            throw new RequestValidationException(ErrorCode.BALANCE_NOT_MATCHED);
        }
        if (trdAmt > maxMnyBlc + custBalance.getPntBlc()) {
            log.info("[checkRealBlc FAIL] 거래금액이 지갑한도+포인트잔액 *초과!*");
            throw new RequestValidationException(ErrorCode.REQ_AMT_NOT_MATCHED);//원거래금액/요청금액을 확인하세요.
        }
        log.info("[checkRealBlc SUCCESS] 요청파라미터 잔액과 실제 잔액 일치로 검증 통과");
    }

}
