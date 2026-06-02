package com.two.backend.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/**
 * Web 层配置，负责开放前端访问后端 API 所需的跨域规则。
 */
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.frontend.allowed-origins}")
    private List<String> allowedOrigins;

    @Override
    /**
     * 为 /api/** 接口配置允许来源、请求方法、请求头和 Cookie 凭证。
     *
     * @param registry Spring MVC 跨域配置注册器
     */
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
