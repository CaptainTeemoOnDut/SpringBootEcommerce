package com.longvo.demo_identity_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisRefreshTokenService {

    RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    private String buildKey(String token) {
        return REFRESH_TOKEN_PREFIX + token;
    }

    public void save(
            String refreshToken,
            Long userId,
            Duration ttl
    ) {
        redisTemplate.opsForValue().set(
                buildKey(refreshToken),
                String.valueOf(userId),
                ttl
        );
    }

    public String getUserIdByRefreshToken(String token) {
        return redisTemplate.opsForValue().get(buildKey(token));
    }

    public void delete(String refreshToken) {
        redisTemplate.delete(buildKey(refreshToken));
    }
}
