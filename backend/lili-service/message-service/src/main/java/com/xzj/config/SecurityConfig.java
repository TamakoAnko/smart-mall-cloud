package com.xzj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 消息服务安全配置：开发阶段暂时放行全部接口，便于前后端联调。
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
                                "/buyer/common/site",
                                "/buyer/common/site/theme",
                                "/buyer/other/pageData/getIndex")
                        .permitAll()
                        .requestMatchers("/buyer/common/slider/**", "/buyer/common/sms/**")
                        .permitAll()
                        .anyRequest().permitAll());
        return http.build();
    }
}