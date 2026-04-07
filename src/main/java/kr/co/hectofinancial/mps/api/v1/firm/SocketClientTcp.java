package kr.co.hectofinancial.mps.api.v1.firm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

@Component
@Slf4j
public class SocketClientTcp {
    private static int FIRM_CONNECT_TIMEOUT;
    private static int FIRM_READ_TIMEOUT;
    private static String SERVER_IP;
    private static int SERVER_PORT;

    @Value("${firm.ip}")
    public void setServerIp(String serverIp) {
        SERVER_IP = serverIp;
    }

    @Value("${firm.port}")
    public void setServerPort(int serverPort) {
        SERVER_PORT = serverPort;
    }

    @Value("${firm.connectTimeout}")
    public void setConnTimeout(int connTimeout) {
        FIRM_CONNECT_TIMEOUT = connTimeout;
    }

    @Value("${firm.readTimeout}")
    public void setReadTimeout(int readTimeout) {
        FIRM_READ_TIMEOUT = readTimeout;
    }

    public static String sendTcp(byte[] data) throws Exception {

        String result = null;
        TcpSocketUtil cst = null;
        ByteArrayOutputStream totalStream = null;
        try {
            cst = new TcpSocketUtil(SERVER_IP, SERVER_PORT, FIRM_CONNECT_TIMEOUT, FIRM_READ_TIMEOUT, "EUC-KR");

            totalStream = new ByteArrayOutputStream();
            cst.sndData(new String(data,  "EUC-KR"));
            result = new String(cst.receiveData(data.length));
        } catch (Exception e) {
            log.info(e.getMessage());
            log.info(Arrays.toString(e.getStackTrace()));
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
    private int connTimeOut = 20000;
    private int readTimeOut = 20000;
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
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
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
