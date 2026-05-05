package com.example.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import com.example.workflow.security.InternalServiceTokenProvider;

import feign.RequestInterceptor;

@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor bearerAuthRequestInterceptor(InternalServiceTokenProvider tokenProvider) {
        return template -> {
            // Do not override an Authorization header set explicitly.
            if (template.headers() != null && template.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
                return;
            }
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getToken());
        };
    }
}

