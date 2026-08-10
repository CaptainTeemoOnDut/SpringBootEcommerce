package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.entity.ProductCategory;
import com.longvo.demo_identity_service.enums.AttributeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttributeResponse {
    private String code;
    private String name;
    private AttributeType type;
    private String unit;
    private boolean required;
}
