[1mdiff --git a/src/main/java/com/nik07roxx/apexPay/Service/Implementation/CustomerServiceImpl.java b/src/main/java/com/nik07roxx/apexPay/Service/Implementation/CustomerServiceImpl.java[m
[1mindex e96cce1..8696535 100644[m
[1m--- a/src/main/java/com/nik07roxx/apexPay/Service/Implementation/CustomerServiceImpl.java[m
[1m+++ b/src/main/java/com/nik07roxx/apexPay/Service/Implementation/CustomerServiceImpl.java[m
[36m@@ -21,6 +21,10 @@[m [mimport com.nik07roxx.apexPay.model.CustomerStatus;[m
 import jakarta.transaction.Transactional;[m
 import lombok.RequiredArgsConstructor;[m
 import lombok.extern.slf4j.Slf4j;[m
[32m+[m[32mimport org.springframework.cache.annotation.CacheEvict;[m
[32m+[m[32mimport org.springframework.cache.annotation.CachePut;[m
[32m+[m[32mimport org.springframework.cache.annotation.Cacheable;[m
[32m+[m[32mimport org.springframework.cache.annotation.EnableCaching;[m
 import org.springframework.data.domain.Page;[m
 import org.springframework.data.domain.Pageable;[m
 import org.springframework.http.HttpStatus;[m
[36m@@ -100,6 +104,7 @@[m [mpublic class CustomerServiceImpl implements CustomerService {[m
     }[m
 [m
     @Override[m
[32m+[m[32m    @Cacheable(value = "customers", key = "#id")[m
     public CustomerResponse findCustomerById(Long id) {[m
         log.info("Finding customer with id: {}.", id);[m
         Customer currentCustomer = customerRepository.findById(id)[m
[36m@@ -121,6 +126,7 @@[m [mpublic class CustomerServiceImpl implements CustomerService {[m
 [m
     @Override[m
     @Transactional[m
[32m+[m[32m    @CacheEvict(value = "customers", key = "#id")[m
     public CustomerResponse updateCustomer(Long id, CustomerCreationRequest customerCreationRequest) {[m
         // validate if customer exists[m
         log.info("Finding customer with id: {} for updation.", id);[m
[36m@@ -158,6 +164,7 @@[m [mpublic class CustomerServiceImpl implements CustomerService {[m
 [m
     @Override[m
     @Transactional[m
[32m+[m[32m    @CacheEvict(value = "customers", key = "#id")[m
     public void deleteCustomerById(Long id) {[m
         // check if customer exists[m
         log.info("Finding customer with id: {} for deletion.", id);[m
[1mdiff --git a/src/main/java/com/nik07roxx/apexPay/config/RedisConfig.java b/src/main/java/com/nik07roxx/apexPay/config/RedisConfig.java[m
[1mindex e99b4e1..a6a08be 100644[m
[1m--- a/src/main/java/com/nik07roxx/apexPay/config/RedisConfig.java[m
[1m+++ b/src/main/java/com/nik07roxx/apexPay/config/RedisConfig.java[m
[36m@@ -1,14 +1,19 @@[m
 package com.nik07roxx.apexPay.config;[m
 [m
 import org.springframework.beans.factory.annotation.Value;[m
[32m+[m[32mimport org.springframework.cache.annotation.EnableCaching;[m
 import org.springframework.context.annotation.Bean;[m
 import org.springframework.context.annotation.Configuration;[m
[32m+[m[32mimport org.springframework.data.redis.cache.RedisCacheConfiguration;[m
[32m+[m[32mimport org.springframework.data.redis.cache.RedisCacheManager;[m
 import org.springframework.data.redis.connection.RedisStandaloneConfiguration;[m
 import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;[m
 import org.springframework.data.redis.core.RedisTemplate;[m
 import org.springframework.data.redis.serializer.StringRedisSerializer;[m
[32m+[m[32mimport java.time.Duration;[m
 [m
 @Configuration[m
[32m+[m[32m@EnableCaching[m
 public class RedisConfig {[m
 [m
     // 1. Manually inject the explicit host, port, and password fields[m
[36m@@ -34,13 +39,24 @@[m [mpublic class RedisConfig {[m
 [m
     // 3. Pass your manually controlled factory straight into the template[m
     @Bean[m
[31m-    public RedisTemplate<String, String> redisTemplate() {[m
[32m+[m[32m    public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory connectionFactory) {[m
         RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();[m
[31m-        redisTemplate.setConnectionFactory(redisConnectionFactory()); // Uses your cloud factory![m
[32m+[m[32m        redisTemplate.setConnectionFactory(connectionFactory); // 👈 Injected parameter![m
 [m
         redisTemplate.setKeySerializer(new StringRedisSerializer());[m
         redisTemplate.setValueSerializer(new StringRedisSerializer());[m
 [m
         return redisTemplate;[m
     }[m
[32m+[m
[32m+[m[32m    @Bean[m
[32m+[m[32m    public RedisCacheManager cacheManager(LettuceConnectionFactory connectionFactory) {[m
[32m+[m[32m        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()[m
[32m+[m[32m                .entryTtl(Duration.ofMinutes(10))[m
[32m+[m[32m                .disableCachingNullValues();[m
[32m+[m
[32m+[m[32m        return RedisCacheManager.builder(connectionFactory) // 👈 Injected parameter![m
[32m+[m[32m                .cacheDefaults(config)[m
[32m+[m[32m                .build();[m
[32m+[m[32m    }[m
 }[m
\ No newline at end of file[m
