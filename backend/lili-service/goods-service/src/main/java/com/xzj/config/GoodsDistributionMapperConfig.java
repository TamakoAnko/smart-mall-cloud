package com.xzj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 扫描 goods-service 内分销业务的 Mapper。
 */
@Configuration
@MapperScan(value = "com.xzj.promotion.distribution.mapper", nameGenerator = GoodsMapperBeanNameGenerator.class)
public class GoodsDistributionMapperConfig {
}