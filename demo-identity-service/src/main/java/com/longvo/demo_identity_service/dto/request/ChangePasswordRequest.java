package com.longvo.demo_identity_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank
    @Size(min = 6, message = "INVALID_PASSWORD")
    String currentPassword;

    @NotBlank
    @Size(min = 6, message = "INVALID_PASSWORD")
    String newPassword;

}
