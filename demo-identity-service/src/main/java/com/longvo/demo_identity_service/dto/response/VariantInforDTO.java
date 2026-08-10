package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.enums.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantInforDTO {
    private Long variantId;
    private BigDecimal price;
    private Integer stock;
}
