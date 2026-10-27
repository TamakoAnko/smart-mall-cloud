package com.xzj.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

/**
 * 自定义 Feign 错误解码：下游返回 4xx/5xx 时，把响应体打进日志，
 * 并给模型一句人话（默认的 FeignException 信息量大且难转述）。
 *
 * 这样以后排查"接口调用失败"就不用再靠猜：AIHelper 控制台里能直接看到下游返回了什么。
 */
@Configuration
public class FeignErrorConfig {

    private static final Logger log = LoggerFactory.getLogger(FeignErrorConfig.class);

    @Bean
    public ErrorDecoder aiFeignErrorDecoder() {
        return (methodKey, response) -> {
            String body = readBody(response);
            log.error("下游接口调用失败 methodKey={} status={} body={}", methodKey, response.status(), body);

            StringBuilder message = new StringBuilder("接口调用失败：HTTP ").append(response.status());
            if (response.status() == 401 || response.status() == 403 || body.contains("服务器繁忙")) {
                message.append("（下游没能识别登录状态或下游自身异常，请提示用户重新登录后重试，不要编造数据）");
            }
            if (!body.isBlank()) {
                message.append(" | 下游返回：").append(body.length() > 300 ? body.substring(0, 300) : body);
            }
            return new IllegalStateException(message.toString());
        };
    }

    private String readBody(Response response) {
        if (response.body() == null) {
            return "";
        }
        try {
            return StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("读取下游响应体失败: {}", e.getMessage());
            return "";
        }
    }
}
