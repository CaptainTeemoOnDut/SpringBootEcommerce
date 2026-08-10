package com.longvo.demo_identity_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreationRequest {

    String name;

    List<ReviewMediaRequest> images;

    ReviewMediaRequest video;

    List<String> removeImages;

    String removeVideo;

    String description;

    List<AttributeValueRequest> categoryDetails;

    List<ProductVariationCreationRequest> variations;

    List<ProductVariantCreationRequest> variants;

    BigDecimal unitPrice;

    Integer unitsInStock;

    Long weight;

    Long width;

    Long length;

    Long height;

    String condition;

    String sku;

    Long categoryId;

    Long shopId;

}

