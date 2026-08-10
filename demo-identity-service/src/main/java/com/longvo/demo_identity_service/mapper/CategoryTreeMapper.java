package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.ProductCategoryCreationRequest;
import com.longvo.demo_identity_service.dto.response.CategoryTreeResponse;
import com.longvo.demo_identity_service.entity.ProductCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryTreeMapper {
    ProductCategory toProductCategory(ProductCategoryCreationRequest request);

    CategoryTreeResponse toCategoryTreeResponse(ProductCategory product);

    //@Mapping(target = "roles", ignore = true)
    //void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);
}
