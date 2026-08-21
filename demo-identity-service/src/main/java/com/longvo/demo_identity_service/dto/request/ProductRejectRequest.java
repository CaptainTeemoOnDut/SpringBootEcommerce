package com.longvo.demo_identity_service.dto.request;

import com.longvo.demo_identity_service.enums.ProductRejectReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRejectRequest {

    @NotBlank
    private ProductRejectReason reason;

    private String note;
}

