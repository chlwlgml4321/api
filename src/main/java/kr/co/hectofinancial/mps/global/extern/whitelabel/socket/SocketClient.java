package kr.co.hectofinancial.mps.global.extern.whitelabel.socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

@Component
@Slf4j
public class SocketClient {
    private static String WL_IP;
    private static int WL_PORT;
    private static int WL_CONN_TIMEOUT;
    private static int WL_READ_TIMEOUT;

    @Value("${whitelabel.ip}")
    public void setWlIp(String wlIp) {
        WL_IP = wlIp;
    }

    @Value("${whitelabel.port}")
    public void setWlPort(int wlPort) {
        WL_PORT = wlPort;
    }

    @Value("${whitelabel.connectTimeout}")
    public void setWlConnTimeout(int wlConnTimeout) {
        WL_CONN_TIMEOUT = wlConnTimeout;
    }

    @Value("${whitelabel.readTimeout}")
    public void setWlReadTimeout(int wlReadTimeout) {
        WL_READ_TIMEOUT = wlReadTimeout;
    }

    public static String sendTcp(byte[] data) throws Exception {

        String result = null;
        TcpSocketUtil cst = null;
        ByteArrayOutputStream totalStream = null;

        try {
            cst = new TcpSocketUtil(WL_IP, WL_PORT, WL_CONN_TIMEOUT, WL_READ_TIMEOUT, "UTF-8");

            totalStream = new ByteArrayOutputStream();
            cst.sndData(new String(data, "UTF-8"));

            result = new String(cst.receiveData(data.length));
        } catch (Exception e) {
            log.error("화이트라벨 연동 실패 sendTcp Error! ", e);
        } finally {
            if (totalStream != null) {
                try {
                    totalStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (cst != null) {
                cst.sockClose();
            }
        }
        return result;
    }
}

/**
 * 소켓연동(전문)
 */
@Slf4j
class TcpSocketUtil {

    private int INTPUTSTREAM_READ_RETRY_COUNT = 10;
    private String strIp = null;
    private int strPort;
    private int connTimeOut = 2000;
    private int readTimeOut = 26000;
    private String charSet = null;
    private Socket soc = null;
    private PrintWriter out = null;
    private InputStream is = null;
    private InputStreamReader isr = null;
    private BufferedReader br = null;


    // socket 생성 & 출력스트림 생성
    public TcpSocketUtil(String ip, int port, int connTimeOut, int readTimeOut, String charSet) throws Exception {
        this.out = null;
        this.soc = null;
        this.strIp = ip;
        this.strPort = port;
        this.connTimeOut = connTimeOut;
        this.readTimeOut = readTimeOut;
        this.charSet = charSet;

        this.soc = new Socket();
        soc.connect(new InetSocketAddress(ip, port), connTimeOut);  //connection timeout

        if (charSet != null && !"".equals(charSet)) {
            this.out = new PrintWriter(new OutputStreamWriter(soc.getOutputStream(), charSet), true);
        } else {
            this.out = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()), true);
        }
    }

    public boolean isConnect() {
        return soc.isConnected();
    }

    // 전문전송
    public void sndData(String sndData) throws Exception {
        soc.setSoTimeout(readTimeOut); // 타임아웃 시간 설정(26초)
        out.print(sndData);
        out.flush();
    }

    //전문 수신부
    public String receiveData(int totalLen) throws Exception {

        StringBuffer buff = new StringBuffer();

        if (strIp == null || strPort == 0) {
            throw new IllegalArgumentException("Parameter 'sendIp, sendPort' is required.");
        }
        BufferedReader br = null;
        if (charSet != null && !"".equals(charSet)) {
            br = new BufferedReader(new InputStreamReader(soc.getInputStream(), charSet));
        } else {
            br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        }

        try {
            char[] buffer = new char[totalLen];
            int charsRead;
            while ((charsRead = br.read(buffer, 0, totalLen)) != -1) {
                buff.append(new String(buffer, 0, charsRead));
            }
        } catch (NumberFormatException ex) {
            buff.setLength(0);
            log.error("화이트라벨 수신 실패 receiveData Error! ", ex);
            throw new Exception();
        } catch (Exception e) {
            log.error("화이트라벨 수신 실패 receiveData Error! ", e);
            throw new Exception();
        } finally {
            this.sockClose();
        }

        return buff.toString();
    }

    public void sockClose() {
        if (br != null) {
            try {
                br.close();
                br = null;
            } catch (Exception ex) {
            }
        }

        if (isr != null) {
            try {
                isr.close();
                isr = null;
            } catch (Exception ex) {
            }
        }

        if (is != null) {
            try {
                is.close();
                is = null;
            } catch (Exception ex) {
            }
        }

        if (out != null) {
            try {
                out.close();
                out = null;
            } catch (Exception ex) {
            }
        }

        if (soc != null) {
            try {
                soc.close();
                soc = null;
            } catch (Exception ex) {
            }
        }
    }

}
