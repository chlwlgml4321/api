package kr.co.hectofinancial.mps.test.encrypt.controller;

import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.test.encrypt.dto.EncryptRequestDto;
import kr.co.hectofinancial.mps.test.encrypt.service.ExceptionTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * TB 서버에서 QA 진행 시, 파라미터 암복호화를 위해 만든 Controller
 */
@Profile({"test", "local"})//TB환경에서만 적용
@RequiredArgsConstructor
@RestController
@Slf4j
public class EncryptController {
    private final CommonService commonService;
    private final ExceptionTestService testService;

    @GetMapping("/test/encrypt")
    public String encrypt(EncryptRequestDto encryptRequestDto) {
        String encrypt = "";

        MarketAddInfoDto marketAddInfoByMId = commonService.getMarketAddInfoByMId(encryptRequestDto.getM_id());
        String encKey = marketAddInfoByMId.getEncKey();
        try {
            encrypt = CipherUtil.encrypt(encryptRequestDto.getMsg(), encKey);
        } catch (Exception e) {
            log.info("{} : ENCRYPT FAIL {}", encryptRequestDto.getMsg(), e.getMessage());
        }
        return encrypt;
    }

    @GetMapping("/test/decrypt")
    public String decrypt(EncryptRequestDto encryptRequestDto) {
        String decrypt = "";

        MarketAddInfoDto marketAddInfoByMId = commonService.getMarketAddInfoByMId(encryptRequestDto.getM_id());
        String encKey = marketAddInfoByMId.getEncKey();

        try {
            String msg = encryptRequestDto.getMsg().replaceAll(" ", "+");
            decrypt = CipherUtil.decrypt(msg, encKey);
        } catch (Exception e) {
            log.info("{} : DECRYPT FAIL {}", encryptRequestDto.getMsg(), e.getMessage());
        }
        return decrypt;
    }

    @PostMapping("/test/hash")
    public String hash(@RequestBody EncryptRequestDto encryptRequestDto) {
        String msg = encryptRequestDto.getMsg();
        try {
            msg = CipherSha256Util.digestSHA256(msg);

        } catch (Exception e) {
            log.info("{} : DECRYPT FAIL {}", msg, e.getMessage());
        }
        return msg;
    }

    @PostMapping("/test/exception")
    public void exceptionTest() {
        try {
            testService.runtimeException();
        } catch (Exception ex) {
            throw new RequestValidationException(ErrorCode.UNDEFINED_SERVER_ERROR_CODE);
        }
    }

    @PostMapping("/test/exception2")
    public void exceptionTest2() {
        try {
            testService.runtimeException();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
