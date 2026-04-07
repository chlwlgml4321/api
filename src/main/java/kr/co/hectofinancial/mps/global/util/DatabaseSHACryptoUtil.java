package kr.co.hectofinancial.mps.global.util;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Entity <-> DataBase Column (SHA-256)
 */
@Slf4j
@Converter
public class DatabaseSHACryptoUtil implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        StringBuffer builder = new StringBuffer();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(attribute.getBytes());
            for (int i = 0; i < bytes.length; i++) {
                int c = bytes[i] & 0xff;
                if (c <= 15) builder.append("0");
                builder.append(Integer.toHexString(c));
            }
        } catch (NoSuchAlgorithmException e) {
            log.info("::DatabaseSHA::  암호화 실패[원인:{}]! 문자열 :{}", e.getMessage(), attribute);
        }
        return builder.toString();
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData;
    }
}
