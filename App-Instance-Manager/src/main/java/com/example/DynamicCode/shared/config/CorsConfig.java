package com.example.DynamicCode.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String FRONTEND_ORIGIN = "http://localhost:5173";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/deploy/network/**")
                .allowedOrigins(FRONTEND_ORIGIN)
                .allowedMethods("GET", "POST");
        registry.addMapping("/api/deploy/server-inventory/**")
                .allowedOrigins(FRONTEND_ORIGIN)
                .allowedMethods("GET");
        registry.addMapping("/api/deploy/server-credentials/**")
                .allowedOrigins(FRONTEND_ORIGIN)
                .allowedMethods("PUT");
        registry.addMapping("/api/inspections/**")
                .allowedOrigins(FRONTEND_ORIGIN)
                .allowedMethods("GET", "POST", "DELETE");
        registry.addMapping("/api/services/**")
                .allowedOrigins(FRONTEND_ORIGIN)
                .allowedMethods("GET", "POST", "DELETE");
    }
}
