package com.two.backend.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
/**
 * Web 层配置，使用 Servlet Filter 级别的 CorsFilter 处理跨域。
 * 相比 WebMvcConfigurer，Filter 在 DispatcherServlet 之前运行，
 * 确保异常处理器返回的错误响应也携带正确的 CORS 头。
 */
public class WebConfig {
    @Value("${app.frontend.allowed-origin-patterns}")
    private List<String> allowedOriginPatterns;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(allowedOriginPatterns);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}

