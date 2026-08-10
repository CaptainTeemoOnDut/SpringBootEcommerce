package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.ProductCreationRequest;
import com.longvo.demo_identity_service.dto.request.ProductUpdateRequest;
import com.longvo.demo_identity_service.dto.response.ProductDetailResponse;
import com.longvo.demo_identity_service.dto.response.ProductResponse;
import com.longvo.demo_identity_service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductCreationRequest request);

    ProductResponse toProductResponse(Product product);

    ProductDetailResponse toProductDetailResponse(Product product);

    @Mapping(target = "id", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);
}
