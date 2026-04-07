package kr.co.hectofinancial.mps.test.encrypt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExceptionTestService {

    public void runtimeException() {
        String message = String.format("**ERROR 발생** 실패 테스트. / " +
                "[ Param 1: param1 " +
                ", Param 2: param2" +
                ", Param 3: param3 " +
                "]");
        throw new RuntimeException(message);
    }
}
