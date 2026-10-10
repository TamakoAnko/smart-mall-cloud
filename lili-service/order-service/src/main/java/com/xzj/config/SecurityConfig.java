package com.xzj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 订单服务安全配置：开发阶段暂时放行全部接口，便于前后端联调。
 *
 * 正式部署时应关闭开发模式，并将鉴权统一放到网关，再按接口补充权限控制。
 */
@Configuration
public class SecurityConfig {

    /**
     * 关闭服务内部默认的 Basic 认证，避免订单服务返回 401 阻断网关请求。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll());
        return http.build();
    }
}
