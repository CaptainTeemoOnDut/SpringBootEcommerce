package com.longvo.demo_identity_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductFilterRequest {

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long categoryId;
    private Double minRating;
}

