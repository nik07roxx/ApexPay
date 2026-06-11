package com.nik07roxx.apexPay.gateway.ratelimiting;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;

    public RateLimitingInterceptor(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. Extract the authenticated username from Spring Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String clientIdentifier;

        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {

            // User is authenticated! Use their unique username as the Redis bucket key
            clientIdentifier = authentication.getName();
        } else {
            // Fallback: If an endpoint is public (like login/register), fall back to IP address
            clientIdentifier = request.getRemoteAddr();
        }

        // 2. Ask Redis if this specific user has tokens left
        boolean isAllowed = rateLimitingService.tryConsume(clientIdentifier);

        // 3. Block them if they are spamming
        if (!isAllowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // HTTP 429
            response.setContentType("application/json");

            response.getWriter().write("{"
                    + "\"status\": 429,"
                    + "\"error\": \"Too Many Requests\","
                    + "\"message\": \"Slow down, bro! You have exhausted your request limit.\""
                    + "}");

            return false; // Stop the request right here!
        }

        return true; // Token deducted, let them pass to the Controller
    }
}