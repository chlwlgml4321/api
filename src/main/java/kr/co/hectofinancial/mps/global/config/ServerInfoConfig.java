package kr.co.hectofinancial.mps.global.config;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ServerInfoConfig {
    public static final String HOST_NAME;
    public static final String HOST_IP;
    public static final String SERVER_INFO;

    static {
        String host = "";
        String ip = "";
        String serverInfo = "";
        try {
            host = InetAddress.getLocalHost().getHostName();
            ip = InetAddress.getLocalHost().getHostAddress();
            serverInfo = "서버 호스트 : " + host + " / 서버 IP: " + ip;
        } catch (UnknownHostException e) {
            host = "Unknown";
            ip = "Unknown";
            serverInfo = "Unknown";
        }
        HOST_NAME = host;
        HOST_IP = ip;
        SERVER_INFO = serverInfo;
    }
}
