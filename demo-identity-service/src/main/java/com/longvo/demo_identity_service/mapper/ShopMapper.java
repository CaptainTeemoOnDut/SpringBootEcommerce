package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.ShopCreationRequest;
import com.longvo.demo_identity_service.dto.request.ShopUpdateRequest;
import com.longvo.demo_identity_service.dto.request.UserCreationRequest;
import com.longvo.demo_identity_service.dto.request.UserUpdateRequest;
import com.longvo.demo_identity_service.dto.response.ShopResponse;
import com.longvo.demo_identity_service.dto.response.UserResponse;
import com.longvo.demo_identity_service.entity.Shop;
import com.longvo.demo_identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShopMapper {
    Shop toShop(ShopCreationRequest request);

    ShopResponse toShopResponse(Shop shop);

    /*@Mapping(target = "roles", ignore = true)
    void updateShop(@MappingTarget Shop shop, ShopUpdateRequest request);*/
}
