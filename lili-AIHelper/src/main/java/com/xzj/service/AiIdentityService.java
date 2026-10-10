package com.xzj.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.xzj.client.AiPassportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

/**
 * 用 accessToken 换会员ID。
 *
 * 注意：这里只缓存 1 分钟。之前缓存 30 分钟会把"token 已经失效"这件事掩盖住 ——
 * 聊天入口看起来正常，但每个工具拿着失效 token 调下游，下游 UserContext 解析不出用户就 NPE，
 * 最终全部返回「服务器繁忙，请稍后重试」。
 */
@Service
public class AiIdentityService {

    private static final Logger log = LoggerFactory.getLogger(AiIdentityService.class);
    private static final String KEY_PREFIX = "lili:ai:identity:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(1);

    private final StringRedisTemplate redis;
    private final AiPassportClient passportClient;

    public AiIdentityService(StringRedisTemplate redis, AiPassportClient passportClient) {
        this.redis = redis;
        this.passportClient = passportClient;
    }

    public String resolveMemberId(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("请先登录再使用 AI 助手");
        }
        String cacheKey = KEY_PREFIX + sha256(accessToken);
        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        JsonNode response;
        try {
            response = passportClient.getCurrentMember(accessToken);
        } catch (Exception e) {
            log.warn("校验登录状态失败（token 可能已过期）: {}", e.getMessage());
            throw new IllegalArgumentException("登录状态校验失败，请重新登录后再试");
        }

        String memberId = response.path("result").path("id").asText("");
        if (memberId.isBlank()) {
            redis.delete(cacheKey);
            throw new IllegalArgumentException("登录状态已失效（token 过期或无效），请重新登录");
        }
        redis.opsForValue().set(cacheKey, memberId, CACHE_TTL);
        return memberId;
    }

    /** 下游返回未授权时调用，主动让身份缓存失效 */
    public void invalidate(String accessToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            redis.delete(KEY_PREFIX + sha256(accessToken));
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("哈希计算失败", e);
        }
    }
}
