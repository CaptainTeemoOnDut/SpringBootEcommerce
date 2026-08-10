package com.longvo.demo_identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {

    //private Long productId;
    private Long variantId;
    private String name;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int quantity;
    private String skuSnapShot;
    private String shopName;
    private Long shopId;
    private boolean selected;

    public CartItemResponse(Long variantId, String name, String imageUrl, BigDecimal unitPrice, int quantity, String skuSnapShot, String shopName, Long shopId) {
        this.variantId = variantId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.skuSnapShot = skuSnapShot;
        this.shopName = shopName;
        this.shopId = shopId;
        this.selected = false;
    }
}
