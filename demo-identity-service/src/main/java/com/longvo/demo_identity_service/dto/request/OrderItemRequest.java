package com.longvo.demo_identity_service.dto.request;

import com.longvo.demo_identity_service.entity.Order;
import com.longvo.demo_identity_service.entity.OrderItem;
import com.longvo.demo_identity_service.enums.OrderItemStatus;
import jakarta.persistence.*;
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
public class OrderItemRequest {


    private Long variantId;

    //String name;

    //private String skuSnapshot; // Color: White + Size: XL


    //private BigDecimal unitPrice;


    private int quantity;

    /*@Enumerated(EnumType.STRING)
    OrderItemStatus status;*/
// PURCHASED, RETURNED, REFUNDED

    //private Long orderId;
}
