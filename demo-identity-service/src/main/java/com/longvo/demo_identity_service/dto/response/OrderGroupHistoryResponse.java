package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderGroupHistoryResponse {
    Long orderGroupId;
    LocalDateTime orderGroupCreatedAt;
    Set<OrderHistoryResponse> orderHistoryResponses;

    public OrderGroupHistoryResponse(Long orderGroupId, LocalDateTime orderGroupCreatedAt) {
        this.orderGroupId = orderGroupId;
        this.orderGroupCreatedAt = orderGroupCreatedAt;
    }
}
