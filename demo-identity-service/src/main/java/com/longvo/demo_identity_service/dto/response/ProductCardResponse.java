package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.enums.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCardResponse {
    Long id;
    String name;

    String thumbnailUrl;   // ảnh chính
    BigDecimal unitPrice;
    Integer soldCount;

    Integer discountPercent = 0; // -12%, -46% (nếu có)
    Boolean isMall;          // Shopee Mall
    Boolean isFavorite = false;      // Yêu thích

    public ProductCardResponse(Long id, String name, String thumbnailUrl, BigDecimal unitPrice, Integer soldCount, Boolean isMall) {
        this.id = id;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.unitPrice = unitPrice;
        this.soldCount = soldCount;
        this.isMall = isMall;
    }

    public ProductCardResponse(Long id, String name, String thumbnailUrl, BigDecimal unitPrice, Integer soldCount) {
        this.id = id;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.unitPrice = unitPrice;
        this.soldCount = soldCount;
        this.isMall = false;
    }
}


