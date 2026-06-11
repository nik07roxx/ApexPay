package com.nik07roxx.apexPay.config;

import com.nik07roxx.apexPay.DTO.Idempotency.IdempotencyRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nik07roxx.apexPay.model.RequestStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";
    private static final String REDIS_PREFIX = "idempotency:";
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public IdempotencyInterceptor(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String key = request.getHeader(IDEMPOTENCY_HEADER);
        if (key == null || key.isBlank()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Missing mandatory X-Idempotency-Key header.");
            return false;
        }

        String redisKey = REDIS_PREFIX + key;

        // 2. Serialize the initial IN_PROGRESS status manually to a plain JSON string
        IdempotencyRecord initialRecord = new IdempotencyRecord(RequestStatus.IN_PROGRESS);
        String initialJson = objectMapper.writeValueAsString(initialRecord);

        // Atomic lock attempt using plain strings
        Boolean isLockAcquired = redisTemplate.opsForValue().setIfAbsent(redisKey, initialJson, LOCK_TIMEOUT);

        if (Boolean.TRUE.equals(isLockAcquired)) {
            request.setAttribute("validIdempotencyKey", redisKey);
            return true;
        }

        // 3. If lock fails, fetch the plain string value and deserialize it manually
        String existingJson = redisTemplate.opsForValue().get(redisKey);

        if (existingJson != null) {
            IdempotencyRecord existingRecord = objectMapper.readValue(existingJson, IdempotencyRecord.class);

            if (existingRecord.getStatus() == RequestStatus.IN_PROGRESS) {
                response.setStatus(HttpStatus.CONFLICT.value());
                response.getWriter().write("An identical request is currently processing. Please wait.");
                return false;
            } else if (existingRecord.getStatus() == RequestStatus.COMPLETED) {
                response.setStatus(existingRecord.getResponseStatusCode());
                response.setContentType("application/json");
                response.getWriter().write(existingRecord.getResponseBody());
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String redisKey = (String) request.getAttribute("validIdempotencyKey");

        if (redisKey != null) {
            int status = response.getStatus();

            if (status >= 200 && status < 500) {
                String mockJsonResponseBody = "{\"message\": \"Transaction completed safely under idempotent token.\"}";

                IdempotencyRecord completedRecord = new IdempotencyRecord(
                        RequestStatus.COMPLETED,
                        status,
                        mockJsonResponseBody
                );

                // 4. Overwrite lock with serialized string
                String completedJson = objectMapper.writeValueAsString(completedRecord);
                redisTemplate.opsForValue().set(redisKey, completedJson, LOCK_TIMEOUT);
            } else {
                redisTemplate.delete(redisKey);
            }
        }
    }
}