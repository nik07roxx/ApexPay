package com.nik07roxx.apexPay.config;

import com.nik07roxx.apexPay.gateway.ratelimiting.RateLimitingInterceptor; // Adjust paths if you move them
import com.nik07roxx.apexPay.gateway.idempotency.IdempotencyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfigurerImpl implements WebMvcConfigurer {

    private final RateLimitingInterceptor rateLimitingInterceptor;
    private final IdempotencyInterceptor idempotencyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // GUARD 1: Rate Limiting executes FIRST to shield the app from traffic spikes/spam.
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/v1/transactions/**");

        // GUARD 2: Idempotency executes SECOND, processing only valid, non-throttled requests.
        registry.addInterceptor(idempotencyInterceptor)
                .addPathPatterns("/api/v1/transactions/**");
    }
}