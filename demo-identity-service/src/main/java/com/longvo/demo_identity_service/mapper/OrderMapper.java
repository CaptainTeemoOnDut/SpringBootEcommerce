package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.OrderCreationRequest;
import com.longvo.demo_identity_service.dto.request.OrderHistoryRequest;
import com.longvo.demo_identity_service.dto.response.OrderCreationResponse;
import com.longvo.demo_identity_service.dto.response.OrderHistoryResponse;
import com.longvo.demo_identity_service.dto.response.OrderPreviewResponse;
import com.longvo.demo_identity_service.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toOrder(OrderCreationRequest request);

    OrderCreationResponse toOrderResponse(Order order);

    OrderPreviewResponse toOrderPreviewResponse(Order order);

    /*@Mapping(target = "id", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);*/
}
