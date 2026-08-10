package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.response.CountryResponse;
import com.longvo.demo_identity_service.dto.response.StateResponse;
import com.longvo.demo_identity_service.entity.Country;
import com.longvo.demo_identity_service.entity.State;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StateMapper {
    //Product toCountry(ProductCreationRequest request);

    StateResponse toStateResponse(State state);

    /*@Mapping(target = "id", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);*/
}
