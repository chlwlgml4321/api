package kr.co.hectofinancial.mps.api.v1.authentication.service;

import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountResponseDto;
import kr.co.hectofinancial.mps.api.v1.authentication.dto.GetBillKeyRequestDto;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.market.domain.MpsMarket;
import kr.co.hectofinancial.mps.api.v1.market.repository.MpsMarketRepository;
import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountRequestDto;
import kr.co.hectofinancial.mps.api.v1.authentication.dto.GetBillKeyResponseDto;
import kr.co.hectofinancial.mps.global.constant.BizDivCd;
import kr.co.hectofinancial.mps.global.constant.PinVerifyResult;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillKeyService {
    @Value("${spring.profiles.active}")
    private String profiles;
    private final AuthenticationService authenticationService;
    private final MpsMarketRepository mpsMarketRepository;

    /* 빌키 조회 */
    public GetBillKeyResponseDto getBillKey(GetBillKeyRequestDto getBillKeyRequestDto) {

        String decBillKey;
        CustomerDto customerDto = getBillKeyRequestDto.customerDto;
        MpsMarket mpsMarket = mpsMarketRepository.findMpsMarketByMid(getBillKeyRequestDto.getMid()).orElseThrow(() -> new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND));
        //custDivCd : I 검증할지말지 TODO

        if ("N".equals(mpsMarket.getBillKeyUseYn())) {
            throw new RequestValidationException(ErrorCode.NOT_VALID_BILL_KEY);
        }

        if (!customerDto.getMCustId().equals(getBillKeyRequestDto.getCustId())) {
            throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        /* 회원 identifier 검증 */
        if (customerDto.getBizDivCd().equals(BizDivCd.INDIVIDUAL.getBizDivCd())) {//개인회원
            if (!customerDto.getCiEnc().equals(getBillKeyRequestDto.getIdentifier())) {//CI 값 검증
                throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND);
            }
        } else if (customerDto.getBizDivCd().equals(BizDivCd.CORPORATE.getBizDivCd()) || customerDto.getBizDivCd().equals(BizDivCd.PERSONAL.getBizDivCd())) {//사업자
            if (!customerDto.getBizRegNo().equals(getBillKeyRequestDto.getIdentifier())) {
                throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND);
            }
        }

        try {
            DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
            decBillKey = databaseAESCryptoUtil.convertToEntityAttribute(customerDto.getBillKeyEnc());

        } catch (Exception e) {
            throw new RequestValidationException(ErrorCode.DECRYPT_ERROR);
        }

        return GetBillKeyResponseDto.builder()
                .custNo(customerDto.getMpsCustNo())
                .billKey(decBillKey)
                .build();
    }

    /* 사용 및 출금 시 빌키 검증 */

    public ChkPinErrorCountResponseDto isCorrectBillkey(ChkPinErrorCountRequestDto chkPinErrorCountRequestDto) {
        log.info("###### CheckBillKey [Start]");

        if (profiles.equals("local") || profiles.equals("dev")) {
            log.info("###### CheckBillKey [End] => Profile not tb or prd");
            return new ChkPinErrorCountResponseDto(PinVerifyResult.SUCCESS);
        }

        CustomerDto customerDto = chkPinErrorCountRequestDto.getCustomerDto();
        String decBillKey = "";

        try {
            DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
            decBillKey = databaseAESCryptoUtil.convertToEntityAttribute(customerDto.getBillKeyEnc());

        } catch (Exception e) {
            log.error("###### CheckBillKey [Error] => billkey decrypt error.. custNo:{}", customerDto.getMpsCustNo());
            throw new RequestValidationException(ErrorCode.DECRYPT_ERROR);
        }

        if (chkPinErrorCountRequestDto.getPin().equals(decBillKey)) {
            log.info("###### CheckBillKey [Success] => custNo:[{}] billkey[{}] is same", customerDto.getMpsCustNo(), chkPinErrorCountRequestDto.getPin());
            return new ChkPinErrorCountResponseDto(PinVerifyResult.SUCCESS);
        }

        log.error("*** CheckBillKey [Error] => custNo:[{}] billkey[{}] Mismatch *** Call [isCorrectWhiteLabelPin]", customerDto.getMpsCustNo(), chkPinErrorCountRequestDto.getPin());
        return authenticationService.isCorrectWhiteLabelPin(chkPinErrorCountRequestDto.setFakePassword());
    }
}
