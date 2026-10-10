package com.xzj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 商品服务安全配置：开发阶段暂时放行全部接口，便于前后端联调。
 */
@Configuration
public class SecurityConfig {

    /**
     * 项目功能完成后，再恢复 CSRF 和 authenticated，并按接口补充白名单。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET,
                                "/buyer/goods/goods",
                                "/buyer/goods/goods/get/**",
                                "/buyer/goods/goods/search",
                                "/buyer/goods/goods/search/related",
                                "/buyer/goods/goods/hot-words",
                                "/buyer/goods/category/get/**",
                                "/buyer/promotion/seckill",
                                "/buyer/promotion/seckill/**")
                        .permitAll()
                        .anyRequest().permitAll());
        return http.build();
    }
}