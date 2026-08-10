package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.OrderHistoryRequest;
import com.longvo.demo_identity_service.dto.request.ProductCreationRequest;
import com.longvo.demo_identity_service.dto.request.ProductUpdateRequest;
import com.longvo.demo_identity_service.dto.response.OrderHistoryResponse;
import com.longvo.demo_identity_service.dto.response.ProductResponse;
import com.longvo.demo_identity_service.entity.Order;
import com.longvo.demo_identity_service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderHistoryMapper {
    Order toOrder(OrderHistoryRequest request);

    OrderHistoryResponse toOrderResponse(Order order);

    /*@Mapping(target = "id", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);*/
}
