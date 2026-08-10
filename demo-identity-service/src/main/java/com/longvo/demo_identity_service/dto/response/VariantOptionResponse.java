package com.longvo.demo_identity_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariantOptionResponse {

    Long id;
    String name; // Color, Size
    Integer sortOrder;
    List<VariantOptionValueResponse> values;

    // ✅ Constructor cho JPQL
    public VariantOptionResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
