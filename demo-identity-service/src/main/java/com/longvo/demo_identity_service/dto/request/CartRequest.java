package com.longvo.demo_identity_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartRequest {

    Long userId;

    @NotEmpty(message = "skuId must not be empty")
    List<Long> skuIds;
}

