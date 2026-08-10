package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.entity.ProductCategory;
import com.longvo.demo_identity_service.enums.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

/*public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal unitPrice,
        String thumbnailUrl,
        ProductStatus status,
        Integer unitsInStock,
        LocalDateTime dateCreated,
        LocalDateTime lastUpdated
) { }*/
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;
    String sku;
    String name;
    String description;
    BigDecimal unitPrice;
    String thumbnailUrl;
    ProductStatus status;
    Integer unitsInStock;
    LocalDateTime dateCreated;
    LocalDateTime lastUpdated;


}
