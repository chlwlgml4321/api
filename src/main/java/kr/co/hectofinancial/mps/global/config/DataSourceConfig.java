//package kr.co.hectofinancial.mps.global.config;
//
//import com.zaxxer.hikari.HikariDataSource;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.mybatis.spring.SqlSessionTemplate;
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//
//import javax.sql.DataSource;
//
////@Configuration
//@Slf4j
////@MapperScan(basePackages = "kr.co.settlebank.b2b.admin.mappers", sqlSessionFactoryRef = "sqlSessionFactory")
//public class DataSourceConfig {
//
////    @Value("${spring.datasource.mapper-location}")
////    private String mapperLocations;
//
////    @Value("${spring.datasource.mybatis-config}")
////    private String configPath;
//
//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource.hikari")
//    public DataSource dataSource() {
//        return DataSourceBuilder.create().type(HikariDataSource.class).build();
//    }
//
//    @Bean
//    public SqlSessionFactory sqlSessionFactory(ApplicationContext applicationContext) throws Exception {
//        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//        sqlSessionFactoryBean.setDataSource(dataSource());
////        sqlSessionFactoryBean.setTypeAliasesPackage("kr.co.settlebank.b2b.commons.models");
////        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources(this.mapperLocations));
////        Resource myBatisConfig = new PathMatchingResourcePatternResolver().getResource(this.configPath);
////        sqlSessionFactoryBean.setConfigLocation(myBatisConfig);
//
//        return sqlSessionFactoryBean.getObject();
//    }
//
//    @Bean
//    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }
//}
