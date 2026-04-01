// [MIGRATION] AS-IS: Nexcore CORS 설정
// [TO-BE]: Spring Boot CORS 설정 (React 17 프론트엔드와 연동)

package com.migration.nexacro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 설정 (React 17 개발 서버와 연동)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000",  // React 17 Vite 개발 서버
                    "http://localhost:5173",  // Vite 기본 포트
                    "http://localhost:8080"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
