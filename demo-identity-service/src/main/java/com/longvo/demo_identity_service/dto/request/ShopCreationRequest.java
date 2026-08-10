package com.longvo.demo_identity_service.dto.request;

import com.longvo.demo_identity_service.validator.DobConstraint;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopCreationRequest {
    @Size(min = 4, message = "NAME_INVALID")
    String name;

    Long userId;

}
