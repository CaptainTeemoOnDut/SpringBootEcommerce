package com.longvo.demo_identity_service.dto;

import com.longvo.demo_identity_service.dto.response.DraftOrderItemRedis;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DraftOrderCreatedEvent {

    private final OrderGroupRedis orderGroupRedis;
    private final String redisKey;

    public DraftOrderCreatedEvent(OrderGroupRedis orderGroupRedis, String redisKey) {
        this.orderGroupRedis = orderGroupRedis;
        this.redisKey = redisKey;
    }

}
