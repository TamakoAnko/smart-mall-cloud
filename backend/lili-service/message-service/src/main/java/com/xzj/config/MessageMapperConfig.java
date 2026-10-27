package com.xzj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 扫描 message-service/message 中已迁移的数据访问层。
 */
@Configuration
@MapperScan(
        value = {"com.xzj.message.mapper", "com.xzj.message.file.mapper"},
        nameGenerator = MessageMapperBeanNameGenerator.class
)
public class MessageMapperConfig {
}
