package com.xzj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 扫描 member-service 中已经迁移到本地的会员数据访问层。
 *
 * 公共模块的 Mapper 仍由公共 MyBatis 配置扫描，这里使用独立 Bean 名称
 * 让本服务可以同时保留兼容层并逐步完成迁移。
 */
@Configuration
@MapperScan(value = "com.xzj.member.mapper", nameGenerator = MemberMapperBeanNameGenerator.class)
public class MemberMapperConfig {
}

