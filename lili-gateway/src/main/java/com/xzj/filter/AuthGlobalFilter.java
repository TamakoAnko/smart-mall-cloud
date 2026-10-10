package com.xzj.filter;

import cn.hutool.core.util.StrUtil;
import cn.lili.cache.CachePrefix;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.token.SecretKeyUtil;
import com.xzj.config.IgnoredUrlsProperties;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final IgnoredUrlsProperties ignoredUrlsProperties;
    private final StringRedisTemplate redisTemplate;

    private static final String USER_CONTEXT_CLAIM = "userContext";
    private static final String HEADER_USER_CONTEXT = "X-User-Context";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        for (String url : ignoredUrlsProperties.getIgnoredUrls()) {
            if (PatternMatchUtils.simpleMatch(url, path)) {
                return chain.filter(exchange);
            }
        }

        String token = request.getHeaders().getFirst(ignoredUrlsProperties.getTokenHeader());
        if (StrUtil.isBlank(token)) {
            return chain.filter(exchange);
        }

        try {
            Claims claims = Jwts.parser().verifyWith(SecretKeyUtil.generalKeyByDecoders()).build()
                    .parseSignedClaims(token).getPayload();
            String userContext = claims.get(USER_CONTEXT_CLAIM).toString();
            AuthUser authUser = new Gson().fromJson(userContext, AuthUser.class);

            Boolean exists = redisTemplate.hasKey(
                    CachePrefix.ACCESS_TOKEN.getPrefix(authUser.getRole(), authUser.getId()) + token);
            // 开发模式允许 token 过期后继续访问，但有效 JWT 仍然要传递用户身份。
            if (!ignoredUrlsProperties.isDevelopmentMode() && !Boolean.TRUE.equals(exists)) {
                return unauthorized(exchange);
            }

            ServerHttpRequest mutated = request.mutate()
                    .header(HEADER_USER_CONTEXT, URLEncoder.encode(userContext, StandardCharsets.UTF_8))
                    .header("X-User-Id", authUser.getId())
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            log.warn("网关收到无效或已过期 Token，拒绝继续转发: {}", e.getClass().getSimpleName());
            // 开发模式允许前端携带残留 Token 联调，避免因旧 Token 直接阻断整站请求。
            // 能解析的 Token 已在上面注入用户上下文，无法解析时则按匿名请求继续转发。
            if (ignoredUrlsProperties.isDevelopmentMode()) {
                return chain.filter(exchange);
            }

            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        String body = "{\"success\":false,\"code\":403,\"message\":\"未登录或token失效\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
