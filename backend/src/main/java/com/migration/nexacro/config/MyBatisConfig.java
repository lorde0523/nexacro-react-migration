// [MIGRATION] AS-IS: Nexcore MyBatis 설정
// [TO-BE]: Spring Boot MyBatis 설정

package com.migration.nexacro.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 설정 (Nexcore DAO 프레임워크 → MyBatis)
 * application.yml에 기본 설정이 포함되어 있어 추가 Bean 등록은 선택사항
 */
@Configuration
@MapperScan("com.migration.nexacro.mapper")
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // Mapper XML 위치 (application.yml과 중복 설정이지만 명시적으로 지정)
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));

        // DTO 타입 별칭 설정 (XML에서 전체 클래스명 대신 단순 클래스명 사용)
        sessionFactory.setTypeAliasesPackage("com.migration.nexacro.dto");

        // MyBatis 설정
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);  // snake_case → camelCase 자동 변환
        config.setDefaultFetchSize(100);
        config.setDefaultStatementTimeout(30);
        sessionFactory.setConfiguration(config);

        return sessionFactory.getObject();
    }
}
