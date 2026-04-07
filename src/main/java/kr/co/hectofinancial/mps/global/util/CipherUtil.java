package kr.co.hectofinancial.mps.global.util;

import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.settlebank.util.Util;
import kr.co.settlebank.util.vo.VoData_in;
import kr.co.settlebank.util.vo.VoData_out;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 요청/응답 파라미터 내 @EncField 붙은 항목의 암복호화 Util
 * Exception 은 모두 throw 하여 사용하는 메서드 내에서 처리
 */
@Slf4j
public class CipherUtil {

    @Getter
    public enum CipherAlgorithm {
        AES_256_ECB_PKCS5Padding("10"),
        AES_128_ECB_PKCS5Padding("20"),
        AES_128_CBC_PKCS5Padding("30"),
        SEED_ECB("40"),
        AES_256_CBC_PKCS5Padding("50");

        private final String value;

        CipherAlgorithm(String value) {
            this.value = value;
        }

        public static CipherAlgorithm find(String value) {
            for (CipherAlgorithm algorithm : CipherAlgorithm.values()) {
                if (algorithm.getValue().equals(value)) {
                    return algorithm;
                }
            }
            throw new RequestValidationException(ErrorCode.CANNOT_FIND_CORRECT_ALGORITHM);
        }
    }

    //기존의 상점별로 암호화방식 및 키값 다를수 있다는 정책에 맞는 암호화 method
//    public static String encrypt(String data, CipherAlgorithm algorithm, String key, String iv) throws Exception {
//        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, algorithm, key, iv);
//        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
//        return Base64.getEncoder().encodeToString(encrypted);
//    }
    /**
     * AES_256_ECB_PKCS5Padding 기본으로 설정된 method
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static String encrypt(String data, String key) throws Exception {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, CipherAlgorithm.AES_256_ECB_PKCS5Padding, key, null);
        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    //기존의 상점별로 암호화방식 및 키값 다를수 있다는 정책에 맞는 복호화 method
//    public static String decrypt(String data, CipherAlgorithm algorithm, String key, String iv) throws Exception {
//        Cipher cipher = getCipher(Cipher.DECRYPT_MODE, algorithm, key, iv);
//        byte[] decodedBytes = Base64.getDecoder().decode(data);
//        byte[] decrypted = cipher.doFinal(decodedBytes);
//        return new String(decrypted, "UTF-8");
//    }

    /**
     * AES_256_ECB_PKCS5Padding 기본으로 설정된 method
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static String decrypt(String data, String key) throws Exception {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE, CipherAlgorithm.AES_256_ECB_PKCS5Padding, key, null);
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        byte[] decrypted = cipher.doFinal(decodedBytes);
        return new String(decrypted, "UTF-8");
    }
    private static Cipher getCipher(int mode, CipherAlgorithm algorithm, String key, String iv) throws Exception {
        String transformation = "";
        int keySize = 0;

        switch (algorithm) {
            case AES_256_ECB_PKCS5Padding:
                transformation = "AES/ECB/PKCS5Padding";
                keySize = 256;
                break;
            case AES_128_ECB_PKCS5Padding:
                transformation = "AES/ECB/PKCS5Padding";
                keySize = 128;
                break;
            case AES_128_CBC_PKCS5Padding:
                transformation = "AES/CBC/PKCS5Padding";
                keySize = 128;
                break;
            case AES_256_CBC_PKCS5Padding:
                transformation = "AES/CBC/PKCS5Padding";
                keySize = 256;
                break;
            case SEED_ECB:
                transformation = "SEED/ECB/PKCS5Padding";
                keySize = 128; // Assuming SEED key size
                break;
        }

        SecretKeySpec keySpec = new SecretKeySpec(getKeyBytes(key, keySize), transformation.split("/")[0]);
        Cipher cipher = Cipher.getInstance(transformation);

        if (transformation.contains("CBC")) {
            IvParameterSpec ivSpec = new IvParameterSpec(getIVBytes(iv));
            cipher.init(mode, keySpec, ivSpec);
        } else {
            cipher.init(mode, keySpec);
        }

        return cipher;
    }

    private static byte[] getKeyBytes(String key, int keySize) throws Exception {
        byte[] keyBytes = new byte[keySize / 8];
        byte[] keyInputBytes = key.getBytes("UTF-8");
        System.arraycopy(keyInputBytes, 0, keyBytes, 0, Math.min(keyInputBytes.length, keyBytes.length));
        return keyBytes;
    }

    private static byte[] getIVBytes(String iv) throws Exception {
        byte[] ivBytes = new byte[16]; // AES block size
        byte[] ivInputBytes = iv.getBytes("UTF-8");
        System.arraycopy(ivInputBytes, 0, ivBytes, 0, Math.min(ivInputBytes.length, ivBytes.length));
        return ivBytes;
    }

}
