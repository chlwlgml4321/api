package kr.co.hectofinancial.mps.global.util;

import kr.co.settlebank.util.Util;
import kr.co.settlebank.util.vo.VoData_in;
import kr.co.settlebank.util.vo.VoData_out;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Entity <-> DataBase Column (AES-256) UTF-8 사용
 * 암복호화 실패시 파라미터로 넘겨준 원래 문자열을 return 하며, 로그 확인 가능
 */
@Slf4j
@Converter
public class DatabaseAESCryptoUtil implements AttributeConverter<String, String> {
    private static final String CHARSET = "UTF-8";
    /**
     * Entity에 들어있는 평문 데이터를 JPA 통해 DB에서 사용시 암호화
     *
     * @param attribute the entity attribute value to be converted
     * @return
     */
    @Override
    public String convertToDatabaseColumn(String attribute) { //암호화
        if (StringUtils.isEmpty(attribute)) {
            return attribute;
        }
        return encryptMagicCrypto(attribute);
    }

    /**
     * magic crypto 를 통해서 암호화된 DB Column 값을 복호화하여 Entity에 셋팅
     *
     * @param dbData
     * @return
     */
    @Override
    public String convertToEntityAttribute(String dbData) { //복호화
        if (StringUtils.isEmpty(dbData)) {
            return dbData;
        }
        return decryptMagicCrypto(dbData);
    }

    private static String encryptMagicCrypto(String src) {
        String encrypt = "";
        if (StringUtils.isEmpty(src)) {
            return src;
        }
        try {
            VoData_in in = new VoData_in();
            in.setsInStr(src.getBytes(CHARSET));
            in.setiInLen(src.getBytes(CHARSET).length);
            in.setiInOutMaxLen(200);

            VoData_out out = Util.STBankEncryptB64(in);
            encrypt = new String(out.getsOutStr(), CHARSET);
//            log.debug("::DatabaseAES:: 문자열 :{} 암호화된 문자열 :{}", src, encrypt);
            return encrypt;
        } catch (Exception e) {
            log.info("::DatabaseAES:: 암호화 실패[원인:{}]! 문자열 :{}", e.getMessage(), src);
            return src;
        }
    }

    private static String decryptMagicCrypto(String src) {
        String decrypt = "";
        if (StringUtils.isEmpty(src)) {
            return src;
        }
        try {
            VoData_in in = new VoData_in();
            in.setsInStr(src.getBytes(CHARSET));
            in.setiInLen(src.getBytes(CHARSET).length);
            in.setiInOutMaxLen(200);

            VoData_out out = Util.STBankDecryptB64(in);
            decrypt = new String(out.getsOutStr(), CHARSET);
//            log.debug("::DatabaseAES:: 문자열 :{} 복호화된 문자열 :{}", src, decrypt);
            return decrypt;
        } catch (Exception e) {
            log.info("::DatabaseAES:: 복호화 실패[원인:{}]! 문자열 :{} ", e.getMessage(), src);
            return src;
        }
    }
}
