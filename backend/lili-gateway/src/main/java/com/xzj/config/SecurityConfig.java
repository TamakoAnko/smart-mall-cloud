package com.xzj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 网关安全配置：开发阶段暂时放行全部接口，便于前后端联调。
 */
@Configuration
public class SecurityConfig {

    /** WebFlux Security needs a reactive CORS source for browser preflight requests. */
    @Bean
    public CorsConfigurationSource gatewayCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(gatewayCorsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(
                                "/buyer/common/site/**",
                                "/buyer/other/pageData/**",
                                "/buyer/common/slider/**",
                                "/buyer/goods/**",
                                "/buyer/promotion/seckill/**",
                                "/buyer/passport/member/pc_session",
                                "/buyer/passport/member/session_login/**",
                                "/buyer/passport/connect/connect/login/web/**",
                                "/buyer/passport/connect/connect/callback/**",
                                "/buyer/passport/connect/connect/result",
                                "/buyer/passport/member/userLogin",
                                "/buyer/passport/member/smsLogin",
                                "/buyer/common/slider/**",
                                "/buyer/common/sms/**")
                        .permitAll()
                        .anyExchange().permitAll())
                .build();
    }
}