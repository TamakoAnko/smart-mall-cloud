package com.xzj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** Scans only the third-party login persistence owned by Passport. */
@Configuration
@MapperScan(value = "com.xzj.passport.connect.mapper", nameGenerator = PassportMapperBeanNameGenerator.class)
public class PassportMapperConfig {
}


