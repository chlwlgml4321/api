package kr.co.hectofinancial.mps.global.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 가맹점의 pktHash 해시처리 SHA-256 hex
 * 조합 필드 : 상점아이디 + 고객번호 + 상점주문번호 + 요청일자 + 요청시간 + 해시키
 * Exception 은 모두 throw 하여 사용하는 메서드 내에서 처리
 */
public class CipherSha256Util {

    public static String digestSHA256(String plain) throws NoSuchAlgorithmException {
        StringBuffer builder = new StringBuffer();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(plain.getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < bytes.length; i++) {
            int c = bytes[i] & 0xff;
            if (c <= 15) builder.append("0");
            builder.append(Integer.toHexString(c));
        }
        return builder.toString();
    }
}
