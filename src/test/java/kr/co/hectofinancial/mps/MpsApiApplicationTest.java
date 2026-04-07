package kr.co.hectofinancial.mps;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = MpsApiApplication.class, properties = {"jasypt.encryptor.password=HectoFinancial_MPS_api", "spring.profiles.active=local"})
@TestPropertySource(locations = "classpath:application-tb.yml")
public class MpsApiApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        if (applicationContext != null) {
            String[] beans = applicationContext.getBeanDefinitionNames();
            for (String bean : beans) {
                System.out.println("bean => " + bean);
            }
        }
    }
}
