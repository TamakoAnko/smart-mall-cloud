package com.xzj.config;

import org.glassfish.jaxb.core.v2.TODO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 服务安全配置：开发阶段暂时放行全部接口，便于前后端联调。
 */
@Configuration
public class SecurityConfig {

    /**
     * 项目功能完成后，再恢复 CSRF 和 authenticated，并按接口补充白名单。
     * TODO
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/buyer/passport/member/pc_session",
                                "/buyer/passport/member/session_login/**",
                                "/buyer/passport/connect/connect/login/web/**",
                                "/buyer/passport/connect/connect/callback/**",
                                "/buyer/passport/connect/connect/result",
                                "/buyer/passport/member/userLogin",
                                "/buyer/passport/member/smsLogin")
                        .permitAll()
                        .anyRequest().permitAll());
        return http.build();
    }
}