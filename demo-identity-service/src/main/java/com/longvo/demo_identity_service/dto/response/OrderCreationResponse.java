package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.dto.request.OrderItemRequest;
import com.longvo.demo_identity_service.enums.PaymentMethods;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreationResponse {
    Long orderId;
    Long userId;
    BigDecimal totalPrice;
    PaymentMethods paymentMethod;
    private Set<OrderItemResponse> orderItems = new HashSet<>();
}
