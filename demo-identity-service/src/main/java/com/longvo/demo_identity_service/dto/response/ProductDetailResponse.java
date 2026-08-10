package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.entity.ProductCategory;
import com.longvo.demo_identity_service.entity.ProductMedia;
import com.longvo.demo_identity_service.entity.Shop;
import com.longvo.demo_identity_service.enums.ProductStatus;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class ProductDetailResponse {

    Long id;
    String name;
    String description;
    BigDecimal unitPrice;
    String thumbnailUrl;
    Integer unitsInStock;

    ShopSummaryResponse shop;
    Long categoryId;
    List<CategoryBreadcrumbResponse> categories;

    // Danh sách các nhóm phân loại: [Màu sắc, Kích thước]
    private List<ProductVariationResponse> variations;

    // Bản đồ tra cứu toàn cục: "101-202" -> 101 thay vì "Đỏ-S" -> 101
    private Map<String, VariantInforDTO> variantLookup;

    public ProductDetailResponse(
            Long id,
            String name,
            String description,
            BigDecimal unitPrice,
            String thumbnailUrl,
            Integer unitsInStock,
            Long shopId,
            String shopName,
            String shopAvatarUrl,
            Long categoryId
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.thumbnailUrl = thumbnailUrl;
        this.unitsInStock = unitsInStock;
        this.shop = new ShopSummaryResponse(shopId, shopName, shopAvatarUrl);
        this.categoryId = categoryId;
    }
}


/*@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailResponse {

    String name;
    String description;
    BigDecimal unitPrice;
    String thumbnailUrl;
    Integer unitsInStock;
    ShopSummaryResponse shop;
    Long categoryId;
    List<CategoryBreadcrumbResponse> categories;

}*/
