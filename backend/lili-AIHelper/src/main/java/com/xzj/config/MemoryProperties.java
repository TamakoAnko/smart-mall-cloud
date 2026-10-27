package com.xzj.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;


import java.time.Duration;

/**
 * 记忆相关配置，对应 application.yaml 里的 lili.ai.memory.*
 *
 * @param triggerMessages 消息条数达到多少时触发摘要压缩
 * @param keepMessages    压缩后保留最近多少条原文
 * @param ttl             Redis 里每条会话记忆的过期时间
 */

@ConfigurationProperties(prefix = "lili.ai.memory")
public record MemoryProperties(
        @DefaultValue("20") int triggerMessages,
        @DefaultValue("6") int keepMessages,
        @DefaultValue("7d") Duration ttl) {
}