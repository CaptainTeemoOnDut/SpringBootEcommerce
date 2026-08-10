package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.ProductCreationRequest;
import com.longvo.demo_identity_service.dto.request.ProductUpdateRequest;
import com.longvo.demo_identity_service.dto.response.CountryResponse;
import com.longvo.demo_identity_service.dto.response.ProductResponse;
import com.longvo.demo_identity_service.entity.Country;
import com.longvo.demo_identity_service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CountryMapper {
    //Product toCountry(ProductCreationRequest request);

    CountryResponse toCountryResponse(Country country);

    /*@Mapping(target = "id", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);*/
}
