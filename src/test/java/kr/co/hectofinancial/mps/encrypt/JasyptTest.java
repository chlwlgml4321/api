package kr.co.hectofinancial.mps.encrypt;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.junit.jupiter.api.Test;

public class JasyptTest {
    @Test
    public void test() {

        String password = "HectoFinancial_MPS_api";

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm("PBEWithMD5AndTripleDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setStringOutputType("base64");
        config.setSaltGenerator(new StringFixedSaltGenerator("someFixedSalt"));
        encryptor.setConfig(config);

        String dbUrl = "jdbc:oracle:thin:@10.86.186.33:1524:DSTDB";
        String dbUsername = "D20230004";
        String dbPassword = "tjsqnf24haley!";

        String encrypt = encryptor.encrypt(dbUrl);
        String encrypt1 = encryptor.encrypt(dbUsername);
        String encrypt2 = encryptor.encrypt(dbPassword);

        System.out.println(encrypt);
        System.out.println(encrypt1);
        System.out.println(encrypt2);
    }
}
