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
public class ProductVariationCreationRequest {

    String code; // color, size
    String name; // Màu sắc, Size

    List<ProductVariationOptionCreationRequest> options;
}

