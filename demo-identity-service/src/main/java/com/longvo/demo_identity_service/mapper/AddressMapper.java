package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.AddressCreationRequest;
import com.longvo.demo_identity_service.dto.request.OrderCreationRequest;
import com.longvo.demo_identity_service.dto.response.OrderCreationResponse;
import com.longvo.demo_identity_service.entity.Address;
import com.longvo.demo_identity_service.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toAddress(AddressCreationRequest request);

    //OrderCreationResponse toOrderResponse(Order order);

    /*@Mapping(target = "id", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);*/
}
