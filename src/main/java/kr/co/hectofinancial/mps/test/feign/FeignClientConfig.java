package kr.co.hectofinancial.mps.test.feign;

import feign.Client;
import feign.Feign;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class FeignClientConfig {

    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .client(new Client.Default(getSSLSocketFactory(), new NoopHostnameVerifier()));
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(5000, 30000);
    }

//   /* @Bean
//    public Encoder feignEncoder() {
//        return new JacksonEncoder();
//    }
//
//    @Bean
//    public Decoder feignDecoder() {
//        return new JacksonDecoder();
//    }*/

    private SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class NoopHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true; // Hostname 검증을 비활성화
        }
    }
}

