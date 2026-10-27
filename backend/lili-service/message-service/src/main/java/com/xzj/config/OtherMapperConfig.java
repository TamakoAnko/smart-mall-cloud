package com.xzj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 扫描 message-service/other 下迁移的页面、系统和直播间 Mapper。
 */
@Configuration
@MapperScan(
        value = {
                "com.xzj.other.page.mapper",
                "com.xzj.other.system.mapper",
                "com.xzj.other.broadcast.mapper"
        },
        nameGenerator = OtherMapperBeanNameGenerator.class
)
public class OtherMapperConfig {
}