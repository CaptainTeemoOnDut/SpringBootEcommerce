package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.PermissionRequest;
import com.longvo.demo_identity_service.dto.request.UserCreationRequest;
import com.longvo.demo_identity_service.dto.request.UserUpdateRequest;
import com.longvo.demo_identity_service.dto.response.PermissionResponse;
import com.longvo.demo_identity_service.dto.response.UserResponse;
import com.longvo.demo_identity_service.entity.Permission;
import com.longvo.demo_identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
