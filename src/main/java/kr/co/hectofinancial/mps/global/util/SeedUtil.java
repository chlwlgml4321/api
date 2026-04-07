package kr.co.hectofinancial.mps.global.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Base64;

@Slf4j
public class SeedUtil {
    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    public static String encrypt(String rawMessage, String key, String iv) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] message = rawMessage.getBytes(EUC_KR);
        byte[] pbszUserKey = key.getBytes();
        byte[] pbszIV = iv.getBytes();
        byte[] encryptedMessage = KISA_SEED_CBC.SEED_CBC_Encrypt(pbszUserKey, pbszIV, message, 0, message.length);
        return new String(encoder.encode(encryptedMessage), EUC_KR);
    }

    public static String decrypt(String encryptedMessage, String key, String iv) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] message = decoder.decode(encryptedMessage);
        byte[] pbszUserKey = key.getBytes();
        byte[] pbszIV = iv.getBytes();
        byte[] decryptedMessage = KISA_SEED_CBC.SEED_CBC_Decrypt(pbszUserKey, pbszIV, message, 0, message.length);
        return new String(decryptedMessage, EUC_KR);
    }
}
