package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String username;
    String avatarUrl;
    String firstName;
    String lastName;
    LocalDate dob;
    String email;
    Set<RoleResponse> roles;
    Boolean noPassword;

}
