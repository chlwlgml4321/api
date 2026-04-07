package kr.co.hectofinancial.mps.global.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Bean(name = "restTemplate")
    public RestTemplate restTemplate() {
        return createRestTemplate(5);
    }

    @Bean(name = "restTemplateBy30")
    public RestTemplate restTemplateBy30() {
        return createRestTemplate(30);
    }

    @Bean(name = "restTemplateBy50")
    public RestTemplate restTemplateBy50() {
        return restTemplateBuilder(50);
    }

    protected RestTemplate createRestTemplate(int timeout) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);


            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000)
                    .setConnectTimeout(timeout * 1000)
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .setDefaultRequestConfig(requestConfig)
                    .build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

            return new RestTemplate(factory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RestTemplate", e);
        }
    }

    @Bean(name = "defaultRetryTemplate")
    public RetryTemplate defaultRetryTemplate() {
        return createRetryTemplate(3);
    }
    
    /**
     * 
     /**
     * description    : 재시도 로직
     * author         : 김혜원
     * date           : 2025-01-14
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2025-01-14        김혜원       최초 생성
     
     * @return
     */
    protected RetryTemplate createRetryTemplate(int count) {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy( count,    //restTemplate 시도횟수
                Collections.singletonMap(RestClientException.class, true));
        
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

    private RestTemplate restTemplateBuilder(int timeout) {
        return new RestTemplateBuilder()
                .defaultHeader("connection", "close") // 명시적으로 keep alive 해제
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .setConnectTimeout(Duration.ofSeconds(timeout))
                .setReadTimeout(Duration.ofSeconds(timeout))
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    public boolean hasError(ClientHttpResponse response) throws IOException {
                        return response.getStatusCode().is5xxServerError();
                    }

                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                        if (response.getStatusCode().is5xxServerError()) {
                            super.handleError(response);
                        }
                    }
                })
                .build();
    }
}

