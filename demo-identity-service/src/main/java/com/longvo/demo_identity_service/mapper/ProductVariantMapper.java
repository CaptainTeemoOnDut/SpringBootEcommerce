package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.ProductCreationRequest;
import com.longvo.demo_identity_service.dto.request.ProductUpdateRequest;
import com.longvo.demo_identity_service.dto.response.ProductDetailResponse;
import com.longvo.demo_identity_service.dto.response.ProductResponse;
import com.longvo.demo_identity_service.dto.response.ProductVariantResponse;
import com.longvo.demo_identity_service.dto.response.VariantInforDTO;
import com.longvo.demo_identity_service.entity.Product;
import com.longvo.demo_identity_service.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
    ProductVariant toProductVariant(ProductVariantResponse productVariantResponse);

    ProductVariantResponse toProductVariantResponse (ProductVariant product);

}
