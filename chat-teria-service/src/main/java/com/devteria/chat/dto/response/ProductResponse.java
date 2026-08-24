package com.devteria.chat.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.devteria.chat.enums.ProductStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
