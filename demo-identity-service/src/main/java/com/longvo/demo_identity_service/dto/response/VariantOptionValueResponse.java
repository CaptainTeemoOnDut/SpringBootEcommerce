package com.longvo.demo_identity_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariantOptionValueResponse {

    Long id;
    String value; // White, XL
    //String imageUrl; // optional
    BigDecimal unitPrice;
    Long optionId;
    //Boolean active;
    //Integer sortOrder;
}
