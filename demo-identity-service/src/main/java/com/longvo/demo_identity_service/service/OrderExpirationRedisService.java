package com.longvo.demo_identity_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.longvo.demo_identity_service.dto.request.CartItemRequest;
import com.longvo.demo_identity_service.dto.response.CartItemResponse;
import com.longvo.demo_identity_service.dto.response.CartResponse;
import com.longvo.demo_identity_service.dto.response.ProductVariantResponse;
import com.longvo.demo_identity_service.entity.CartItem;
import com.longvo.demo_identity_service.entity.ProductVariant;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.mapper.ProductVariantMapper;
import com.longvo.demo_identity_service.repository.CartItemRepository;
import com.longvo.demo_identity_service.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderExpirationRedisService {

    private static final String ORDER_EXPIRATION_KEY = "order_expiration";
    private static final String ORDER_ITEM_EXPIRATION_KEY = "order_item_expiration";

    private final RedisTemplate<String, String> redisTemplate;

    public void scheduleOrderExpiration(String orderId, long expireTimestamp) {

        redisTemplate.opsForZSet()
                .add(ORDER_EXPIRATION_KEY, orderId, expireTimestamp);
    }

    public Set<String> getExpiredOrders(long now, int limit) {

        return redisTemplate.opsForZSet()
                .rangeByScore(ORDER_EXPIRATION_KEY, 0, now, 0, limit);
    }

    public Set<String> getExpiredOrderItems(long now, int limit) {

        return redisTemplate.opsForZSet()
                .rangeByScore(ORDER_ITEM_EXPIRATION_KEY, 0, now, 0, limit);
    }

    public void removeOrder(String orderId) {

        redisTemplate.opsForZSet()
                .remove(ORDER_EXPIRATION_KEY, orderId);
    }
}

