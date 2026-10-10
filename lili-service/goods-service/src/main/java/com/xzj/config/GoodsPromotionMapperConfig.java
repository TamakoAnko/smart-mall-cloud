package com.xzj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * goods-service 专用的促销 Mapper 扫描配置。
 *
 * 促销业务已迁移到本服务，但公共模块中的旧 Mapper 仍保留给其他服务使用。
 */
@Configuration
@MapperScan(value = "com.xzj.promotion.promotion.mapper", nameGenerator = GoodsMapperBeanNameGenerator.class)
public class GoodsPromotionMapperConfig {
}
