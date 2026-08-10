package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.RoleRequest;
import com.longvo.demo_identity_service.dto.request.UserCreationRequest;
import com.longvo.demo_identity_service.dto.request.UserUpdateRequest;
import com.longvo.demo_identity_service.dto.response.RoleResponse;
import com.longvo.demo_identity_service.dto.response.UserResponse;
import com.longvo.demo_identity_service.entity.Role;
import com.longvo.demo_identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);

}
