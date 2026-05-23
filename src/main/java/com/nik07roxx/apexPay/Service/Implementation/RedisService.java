package com.nik07roxx.apexPay.Service.Implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, String> redisTemplate;

    public <T>T get(String key, Class<T> entityClass)
    {
        try{
            String jsonStr = redisTemplate.opsForValue().get(key);
            if (jsonStr == null) {
                return null; // Handle cache miss safely so you don't get NullPointerExceptions
            }
            return objectMapper.readValue(jsonStr, entityClass);
        } catch (Exception e) {
            log.error("Exception during Redis cache retrieval: ", e);
            return null;
        }
    }

    public void set(String key, Object o, Long ttl)
    {
        try{
            String jsonValue = objectMapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Exception during Redis cache storage: ", e);
        }
    }
}
