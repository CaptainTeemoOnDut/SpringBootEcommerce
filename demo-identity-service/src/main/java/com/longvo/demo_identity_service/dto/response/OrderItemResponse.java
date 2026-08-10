package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.enums.OrderItemStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {

    Long orderId;

    private Long orderItemId;

    private Long variantId;

    private String variantName;

    private String imageUrl;

    private String skuSnapshot; // Color: White + Size: XL


    private BigDecimal unitPrice;

    private BigDecimal lineTotal;

    private Integer quantity;

    //@Enumerated(EnumType.STRING)
   // OrderItemStatus status;
// PURCHASED, RETURNED, REFUNDED



    //private Long orderId;
}
