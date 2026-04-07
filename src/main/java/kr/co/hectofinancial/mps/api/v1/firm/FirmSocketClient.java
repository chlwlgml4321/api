package kr.co.hectofinancial.mps.api.v1.firm;

import kr.co.hectofinancial.mps.global.extern.whitelabel.socket.SocketFieldBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FirmSocketClient {

    public static String sendSocket(String encRespData) {

        encRespData = "0408" + encRespData;
        String response = null;
        try {
            response = SocketClientTcp.sendTcp(SocketFieldBuilder.getPaymentPinField2(encRespData, null, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Response from server: " + response);
        return response;
    }

}