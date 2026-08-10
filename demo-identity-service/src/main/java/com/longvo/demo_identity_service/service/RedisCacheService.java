package com.longvo.demo_identity_service.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j // Tương đương với ILogger trong .NET
@RequiredArgsConstructor // Tự động tạo Constructor cho các field final
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper; // Tương đương JsonSerializer

    public <T> Optional<T> getAsync(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }

            log.debug("Cache HIT for key: {}", key);
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (Exception e) {
            log.warn("Redis GET failed for key: {}. Falling through to source.", key, e);
            return Optional.empty();
        }
    }

    public <T> void setAsync(String key, T value, Duration expiration) {
        try {
            String json = objectMapper.writeValueAsString(value);
            // Nếu expiration null, mặc định là 24 giờ
            Duration ttl = (expiration != null) ? expiration : Duration.ofHours(24);

            redisTemplate.opsForValue().set(key, json, ttl);
            log.debug("Cache SET for key: {}, TTL: {}", key, ttl);
        } catch (JsonProcessingException e) {
            log.warn("Redis SET failed for key: {}. Continuing without cache.", key, e);
        }
    }

    public void removeAsync(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis REMOVE failed for key: {}.", key, e);
        }
    }
}
