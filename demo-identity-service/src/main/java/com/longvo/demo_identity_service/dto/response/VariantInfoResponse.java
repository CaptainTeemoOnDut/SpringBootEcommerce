package com.longvo.demo_identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantInfoResponse {
    private Long variantId;
    private BigDecimal price;
    private Integer stock;
}
