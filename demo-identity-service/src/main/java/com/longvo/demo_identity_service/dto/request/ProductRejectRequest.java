package com.longvo.demo_identity_service.dto.request;

import com.longvo.demo_identity_service.enums.ProductRejectReason;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRejectRequest {

    private ProductRejectReason reason;

    private String note;
}

