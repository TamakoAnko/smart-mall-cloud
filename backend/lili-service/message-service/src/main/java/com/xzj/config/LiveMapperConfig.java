package com.xzj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 扫描 message-service/live 中已迁移的数据访问层。
 */
@Configuration
@MapperScan(value = "com.xzj.live.mapper", nameGenerator = LiveMapperBeanNameGenerator.class)
public class LiveMapperConfig {
}