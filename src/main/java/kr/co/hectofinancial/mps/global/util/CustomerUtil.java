package kr.co.hectofinancial.mps.global.util;

import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.global.constant.BizDivCd;
import kr.co.hectofinancial.mps.global.constant.CustStatCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class CustomerUtil {

    /*요청 파라미터 내 ci값과 회원정보 ci값 확인*/
    public static void checkValidCustomerByCi(String reqCiEnc, CustomerDto customerDto) {
        if (!CustStatCd.WITHDRAW.getStatCd().equals(customerDto.getStatCd()) && StringUtils.isNotBlank(reqCiEnc)) {
            //상태값 "해지" 아니면 ci 값 비교
            if (BizDivCd.CORPORATE.getBizDivCd().equals(customerDto.getBizDivCd()) || BizDivCd.PERSONAL.getBizDivCd().equals(customerDto.getBizDivCd())) {
                //법인 || 개인 (사업자)
                if (!customerDto.getBizRegNo().equals(reqCiEnc)) {
                    log.info("***** 사업자 CI값(사업자등록번호) 불일치=> DB [{}] 요청값 [{}]", customerDto.getBizRegNo(), reqCiEnc);
                    throw new RequestValidationException(ErrorCode.BIZ_REG_NO_NOT_MATCH);
                }
            } else {
                //개인
                if (!customerDto.getCiEnc().equals(reqCiEnc)) {
                    log.info("*****CI값 불일치=> DB CI [{}] 요청값 [{}]", customerDto.getCiEnc(), reqCiEnc);
                    throw new RequestValidationException(ErrorCode.CI_NOT_MATCH);
                }
            }
        }
    }
}
