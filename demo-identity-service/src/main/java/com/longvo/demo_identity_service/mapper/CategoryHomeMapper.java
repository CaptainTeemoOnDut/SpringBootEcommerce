package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.ProductCategoryCreationRequest;
import com.longvo.demo_identity_service.dto.response.CategoryHomeResponse;
import com.longvo.demo_identity_service.dto.response.CategoryTreeResponse;
import com.longvo.demo_identity_service.entity.ProductCategory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryHomeMapper {
    ProductCategory toProductCategory(ProductCategoryCreationRequest request);

    List<CategoryHomeResponse> toHomeResponses(List<ProductCategory> categories);

    //@Mapping(target = "roles", ignore = true)
    //void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);
}
