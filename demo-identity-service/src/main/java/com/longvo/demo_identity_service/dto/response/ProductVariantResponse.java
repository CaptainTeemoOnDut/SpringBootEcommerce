package com.longvo.demo_identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    private Long variantId;
    private String name;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int stock;
    private String sku;
    private String shopName;
    private Long shopId;
}
