package com.xzj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** Registers the remaining wechat data access classes owned by message-service. */
@Configuration
@MapperScan("com.xzj.other.wechat.mapper")
public class LocalModulesMapperConfig {
}
