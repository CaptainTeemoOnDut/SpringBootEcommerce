package com.longvo.demo_identity_service.dto.request;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String firstName;
    String lastName;
    LocalDate dob;
    List<String> roles;
}
