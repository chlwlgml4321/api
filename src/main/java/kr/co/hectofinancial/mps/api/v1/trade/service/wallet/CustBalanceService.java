package kr.co.hectofinancial.mps.api.v1.trade.service.wallet;

import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.CustomerWalletResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlc.GetBlcIn;
import kr.co.hectofinancial.mps.api.v1.trade.procedure.GetBlc.GetBlcOut;
import kr.co.hectofinancial.mps.api.v1.trade.repository.CustWlltRepository;
import kr.co.hectofinancial.mps.global.constant.ProcResCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustBalanceService {

    private final CustWlltRepository custWlltRepository;

    /**
     * customerDto 재조회 없이 심플하게 잔액 조회
     * @param custNo
     * @param chargeLimit
     * @return
     */
    public CustomerWalletResponseDto getBalanceByCustomer(String custNo, long chargeLimit) {
        GetBlcIn getBlcIn = GetBlcIn.builder()
                .inMpsCustNo(custNo)
                .inChrgLmtAmt(chargeLimit)
                .build();
        GetBlcOut custWlltBlc = custWlltRepository.getBlc(getBlcIn);
        long resCode = (long) custWlltBlc.getOutResCd();
        String resMsg = custWlltBlc.getOutResMsg();

        if (resCode != ProcResCd.SUCCESS.getResCd()) {
            log.info("잔액조회 실패 => 회원번호 :{} 에러메세지: {}", custNo, resMsg);
            throw new RequestValidationException(ErrorCode.GET_BALANCE_ERROR);
        }

        CustomerWalletResponseDto customerWalletResponseDto = CustomerWalletResponseDto.builder()
                .custNo(custNo)
                .mnyBlc((Long) custWlltBlc.getOutMnyBlc())
                .pntBlc((Long) custWlltBlc.getOutPntBlc())
                .build();
        return customerWalletResponseDto;
    }
}
