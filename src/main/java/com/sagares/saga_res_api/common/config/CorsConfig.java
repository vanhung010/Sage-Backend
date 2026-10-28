package com.sagares.saga_res_api.common.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**") // ap dung tat ca endpoint
                        .allowedOrigins(
                                "http://localhost:5000"
                        ) // cho phep cac http
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*") // cho phep tat ca header (auth, content-type)
                        .exposedHeaders("Authorization", "Content-Disposition") // header co the doc duoc
                        .allowCredentials(true) // cho phep gui cookie hoac header auth
                        .maxAge(3600); // cache ket qua preflight request trong 1 gio
            }
        };
    }
}
