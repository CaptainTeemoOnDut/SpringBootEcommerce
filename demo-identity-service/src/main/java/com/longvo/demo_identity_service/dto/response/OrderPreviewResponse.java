package com.longvo.demo_identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPreviewResponse {
    Long orderId;
    List<ShopOrderPreview> shops;
    Long addressId;
    String fullAddress;
    String receiverName;
    String phoneNumber;
    List<OrderItemResponse> orderItemResponses;
    BigDecimal subTotal;
    BigDecimal discountAmount;
    BigDecimal finalTotal;
}
