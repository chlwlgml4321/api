package kr.co.hectofinancial.mps.global.config.jpa;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    @Bean
    @ConditionalOnMissingBean(name = "jpaAuditingHandler")
    public JpaAuditorAwareImpl auditorAware() {
        return new JpaAuditorAwareImpl();
    }
    /**
     * Jeus 서버 일 경우, Transaction Manager 를 명시하지 않을 경우, JTA (제우스 트랜잭션 매니저) 사용하기때문에
     * Procedure 호출 후 @Transactional 이 발생하지 않는 오류발생 할 수 있음 => JPA 트랜잭션 사용하는 것으로 명시 
     * @param entityManagerFactory
     * @return
     */    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
