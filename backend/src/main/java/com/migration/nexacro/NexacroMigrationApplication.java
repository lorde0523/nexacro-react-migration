// [MIGRATION] AS-IS: Nexcore Application Entry Point
// [TO-BE]: Spring Boot Main Application

package com.migration.nexacro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Nexacro 14 → React 17 + Spring Boot 마이그레이션 메인 애플리케이션
 *
 * AS-IS: Nexcore Framework 기반 WAS
 * TO-BE: Spring Boot Embedded Tomcat (포트 8080)
 */
@SpringBootApplication
public class NexacroMigrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexacroMigrationApplication.class, args);
    }
}
