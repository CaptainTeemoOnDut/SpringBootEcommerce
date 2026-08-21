package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.response.ProductVariantResponse;
import com.longvo.demo_identity_service.entity.ProductVariant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
    ProductVariant toProductVariant(ProductVariantResponse productVariantResponse);

    ProductVariantResponse toProductVariantResponse (ProductVariant product);

}
