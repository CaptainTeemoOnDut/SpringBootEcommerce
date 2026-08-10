package com.longvo.demo_identity_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisRefreshTokenService {

    RedisTemplate<String, String> redisTemplate;

    public String getRefreshToken(String token) {
        // hoặc return token nếu chỉ cần xác thực tồn tại trong Redis
        return redisTemplate.opsForValue().get("refresh:" + token);
    }

    public void save(String refreshToken, Long userId) {
        redisTemplate.opsForValue().set("refresh:" + refreshToken, String.valueOf(userId), Duration.ofDays(7));

        //***Lưu ý: Đây là đoạn code đã gây ra bottleneck cho hàm refresh token
        /*Set<String> keys = redisTemplate.keys("*");
        for (String key : keys) {
            String value = redisTemplate.opsForValue().get(key);
            System.out.println("📝 " + key + " = " + value);
        }*/

    }

    public String getUserIdByRefreshToken(String refreshToken) {
        return redisTemplate.opsForValue().get("refresh:" + refreshToken);
    }

    public void delete(String refreshToken) {
        redisTemplate.delete("refresh:" + refreshToken);
    }
}
