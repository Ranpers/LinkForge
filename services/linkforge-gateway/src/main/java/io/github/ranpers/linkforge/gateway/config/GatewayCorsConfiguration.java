package io.github.ranpers.linkforge.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GatewayCorsProperties.class)
public class GatewayCorsConfiguration {

    @Bean
    CorsConfigurationSource gatewayCorsConfigurationSource(GatewayCorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        // Origin 已精确限制；允许浏览器声明业务所需请求头，避免新增请求头时破坏预检。
        configuration.setAllowedHeaders(List.of("*"));
        // 限流响应头取自 RedisRateLimiter 的常量而非字面量：一旦上游改名，这里会编译失败，
        // 而不是静默暴露一个浏览器永远读不到的请求头。
        configuration.setExposedHeaders(List.of(
                "Location",
                GatewayRequestIdWebFilter.HEADER_NAME,
                RedisRateLimiter.REMAINING_HEADER,
                RedisRateLimiter.REPLENISH_RATE_HEADER,
                RedisRateLimiter.BURST_CAPACITY_HEADER,
                RedisRateLimiter.REQUESTED_TOKENS_HEADER
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
