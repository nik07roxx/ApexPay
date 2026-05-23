package com.nik07roxx.apexPay.Service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTesting {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void testRedisCaching()
    {
        // redisTemplate.opsForValue().set("email","nikhil@email.com");
        Object email = redisTemplate.opsForValue().get("email");
        int a = 1;
    }
}
