package com.xzj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 扫描 message-service/common 中已迁移的数据访问层。
 *
 * 公共模块的旧 Mapper 仍由兼容配置扫描，本配置使用独立名称让两套代码可以并存。
 */
@Configuration
@MapperScan(
        value = {
                "com.xzj.common.setting.mapper", "com.xzj.common.region.mapper",
                "com.xzj.common.file.mapper",
                "com.xzj.common.verification.mapper"
        },
        nameGenerator = CommonMapperBeanNameGenerator.class
)
public class CommonMapperConfig {
}