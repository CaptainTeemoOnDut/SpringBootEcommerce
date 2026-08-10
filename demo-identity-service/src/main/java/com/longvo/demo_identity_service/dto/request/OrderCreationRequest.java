package com.longvo.demo_identity_service.dto.request;

import com.longvo.demo_identity_service.entity.OrderItem;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreationRequest {
    private Set<OrderItemRequest> orderItems = new HashSet<>();
}
