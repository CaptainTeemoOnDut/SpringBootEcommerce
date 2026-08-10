package com.longvo.demo_identity_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemRequest {

    @NotNull(message = "Sku id must not be empty")
    Long skuId;

    @Min(1)
    int quantity;

    boolean isReplace;
}

