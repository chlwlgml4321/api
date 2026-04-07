package kr.co.hectofinancial.mps.encrypt;

import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static kr.co.hectofinancial.mps.global.util.CipherSha256Util.*;
import static kr.co.hectofinancial.mps.global.util.CipherUtil.decrypt;
import static kr.co.hectofinancial.mps.global.util.CipherUtil.encrypt;

public class ParameterEncTest {
    //ST1803211422454087540
    String M2417525 = "000i2m0640zg8KEIWStnlz0CAVP61pCL"; //DSTDB 의 MID = M2417525 의 AES 암호화키
    String M2471645 = "SETTLEBANKISGOODSETTLEBANKISGOOD"; //TSTDB 의 MID = M2471645 의 AES 암호화키
    String M2485302 = "561hR351U5r6B0HUCJk50p0MNbIXJ4RY"; //PSTDB 의 MID = M2485302
    String M2485302_Hash = "ST1904151719051154188"; //PSTDB 의 MID = M2485302 의 해시키
    String M24A6602 = "qjbFh21IG2h9IGB8MwF11PHf3sv5xCVN";//프리페이드 운영
    String M24c4177 = "SETTLEBANKISGOODSETTLEBANKISGOOD";//RRP
    String paystbd2 = "pgSettle30y739r82jtd709yOfZ2yK5K";//RRP
    String M2548094 = "UUQC71MuIEBNT667MpW3BUVhF01e3Y44";//부릉 운영
    String rrPrd = "o9lp5R30y739r82jtd709yOfZ2yK5Keo";//라운드 운영
    String BpoTest = "EBL1PBBuW3538PiZ264eP81A3BfU80R0";
    String M257833 = "o9lp5R30y739r82jtd709yOfZ2yK5Keo"; //와우플렉스 운영
    String CI = "VK3LFo5yI/K0skiLk1GBwKyulKXevTkZolY7ACtrXBxNTBXfJWicsADi/DIOD6AYWbNHxK9HCXJwOzN4fEnYtQ==";

    @Test
    public void test1() throws Exception {

        // 암호화
        String decAmt = "120021";
        String aesAmt = getEncVal(decAmt);
        System.out.println(aesAmt);

        decAmt = CI;
        aesAmt = getEncVal(decAmt);
        System.out.println(aesAmt);

        decAmt = "1029992";
        aesAmt = getEncVal(decAmt);
        System.out.println(aesAmt);

        decAmt = "10000";
        aesAmt = getEncVal(decAmt);
        System.out.println(aesAmt);

        decAmt = "1029992";
        aesAmt = getEncVal(decAmt);
        System.out.println(aesAmt);

        decAmt = "100";
        aesAmt = getEncVal(decAmt);
        System.out.println(aesAmt);
        // 복호화
//        aesAmt = "JCfqhMJx5cmLFSZD+Tbu3ZHxfaVRJAEcpokyKT+lUVW8zmq9b9t30Yux3/5QL42bKtRMiLb2sokj0HJwWtJtWv/i/H8vIowUk0aZH7uRaAtwugngWhXA8As9GeVDzWMYr7OcpfSXRub7xoCdL6mwt7O+5mQt3z+2T8Ij5H7rPysW2sQF01c3EHcZNbqz+4kmpv2zA9eLl0M2VusOcBlksP5pEZxuMv3KgY7vYWLv4KEnUeWtZWCv5mtsurjGZd820RAwkP89WomZuW7UrJyBLswLMKfkXMyT0fWTA+q7e7kfGzDFxqB08FyYPlhyhPbFOMuLLuoP9U9QDjCZhRtlKkU+/3HGNkBLG0caNS1S3II/9dN1wM8hlpGw+Iv2L65cw4OgTqvU4fj0CtbXot3YVVvX8EjY2n8Ybt34DqKYC5eRCNOIkVzfxKh2e5hXqYjEj+2MYXRlC+wLtXamKdW2CkvRVOr4En67JKm2a4DKogrKX5P76TEhGDPI5B1wnPRsxqsXa6H9m6NU+qiXflEICAHtPsjBIhcXVvgHHVVbUqK4UlSQBaFijUpgWY1WaEMlI06GOab3jXTKhNxEy6K2YEvz2SdpU58dvqF1qJ6ZCadpe+e2qQj+LYhG0+FO5OOGos/AtUeWR981nhoUo7hZFOMqnmoAZKUcC68XJpGSYFNyyBEg3pn53+S4M2PmiZKwA+mGBZCxY5jZggYyYyyGpkMU/BbH/E/P3/JQeQp1TPxZV7S8XMD9JROoASTWimTVHr4ok9K77ML9AYW0AplE4PiYvNeWRg1W6vBZAU46to7v6Wxqzrw+oqBuh52p8/oMZeViCTk9xqhGvgg3aymTffi6xDAifyOSr4gUlOkJCmxYui/Xz9T2rty6wKH+cKt4DVprqbDHisEqczodhHa2YfEPNvIfyhvdveAY1fybKaMoWPdWgzvMm7xsSBo6zfplA0z3ptFqrOjHks3Ww4VgqUD6DmJQ1kk3NW1GIcJs0yNsBys2e36X6GX5jVWvonki4W34BH8lhMLbZ7RDK5k+EIU6TEJ4VehcDvyRY8T1WsXq1ZPofc323yUd8nsCu68o6uRks/Q0yUmcGWINEMURJhxHKtIjOYBuB4fbfo8e+aq4x7eug+iClF/XSSa+aItWI8XIM0+odeFCQo5T1WFBvGkNMXvpzPWoCK25hj3+Lf5HN5z2vEY04ht12jOin4nwNzQH3eQpGpU0PcG3Yan56T3NVo/7leRxTz0F9EfjTU8Hsj2y964ccxefmHhvD55A";
//        System.out.println("복호화:" + getDecVal(aesAmt));
        aesAmt = "yeUsW/4I4JlTVN7cEqHziA==";
//        System.out.println(getDecVal(aesAmt));
        aesAmt = "YjFnSdsH7mzB+pQlvl9oNQ==";
//        System.out.println(getDecVal(aesAmt));
//        aesAmt = "OtHHsG793ox9XewbvX21Lw==";
//        System.out.println(getDecVal(aesAmt));
//        aesAmt = "rQkPiWdzsPVMMLrCvMPD8g==";
//        System.out.println(getDecVal(aesAmt));
//        aesAmt = "rDcuWDKTUbvBZR2z2yAvWg==";
//        System.out.println(getDecVal(aesAmt));
//        aesAmt = "Zs6Dp8MI6dQOfGmuTr5FFJjkFhloOmGAJWxTZ+9XHzI=";
//        System.out.println(getDecVal(aesAmt));
//        aesAmt = "CbNBqT+yLOEkb9U/jKM0RQ==";
//        System.out.println(getDecVal(aesAmt));
//        aesAmt = "JZSG6GbpDd7bCta8cZlm8g==";
//        System.out.println(getDecVal(aesAmt));
        //{T} 암호화
        DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
        String aesStr = databaseAESCryptoUtil.convertToDatabaseColumn("4d26a2c6-814a-73f5-cdf1-7af84e07bad2");
        System.out.println("{T} 암호화: " + aesStr);

        // {T} 복호화
        String aes = "4d26a2c6-814a-73f5-cdf1-7af84e07bad2";
        String s = new DatabaseAESCryptoUtil().convertToEntityAttribute(aes);
        System.out.println("{T} 복호화: " + s);


        // pktHash
        String pktHashStr = "2400024945" + "Ziheeee" + "25103110571100250765" + "APPROVAL" + "1" + "0";
        String pktHash = digestSHA256("ST1803211422454087540");
        System.out.println("pktHash: " + pktHash);
//        String test = "2400000979M2485302TEST0022502130912380002030310000ST2411011342195469789";
//        CipherSha256Util.digestSHA256(test.toString());
//
//        System.out.println(",,,CipherSha256Util.digestSHA256(test.toString())," +CipherSha256Util.digestSHA256(test.toString()));

    }

    private String getEncVal(String str) throws Exception {
        return encrypt(str, M2485302);
    }

    private String getDecVal(String str) throws Exception {

        return decrypt(str, M2485302);
    }

    private String makePktHash(String... args) throws NoSuchAlgorithmException {
        String pktHash = "";
        for (String arg : args) {
            pktHash += arg;
        }
        return digestSHA256(pktHash);
    }
}
