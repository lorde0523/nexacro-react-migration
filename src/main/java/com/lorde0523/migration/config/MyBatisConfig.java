package com.lorde0523.migration.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.lorde0523.migration")
public class MyBatisConfig {
}
