package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.entity.ProductCategory;
import com.longvo.demo_identity_service.enums.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductTableRowResponse{
    Long id;
    String name;
    String sku;
    ProductStatus status;
    BigDecimal unitPrice;
    Integer unitsInStock;
}

