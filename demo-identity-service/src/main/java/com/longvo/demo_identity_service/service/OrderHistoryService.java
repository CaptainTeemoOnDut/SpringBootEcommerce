package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.response.OrderHistoryResponse;
import com.longvo.demo_identity_service.dto.response.OrderItemResponse;
import com.longvo.demo_identity_service.entity.Order;
import com.longvo.demo_identity_service.enums.OrderStatus;
import com.longvo.demo_identity_service.mapper.OrderHistoryMapper;
import com.longvo.demo_identity_service.repository.OrderItemRepository;
import com.longvo.demo_identity_service.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderHistoryService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderHistoryMapper orderHistoryMapper;

    public Page<OrderHistoryResponse> getOrdersByUserEmail(String userEmail, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findByUserEmail(userEmail, pageable);
        return ordersPage.map(orderHistoryMapper::toOrderResponse);
    }

    /*public List<OrderHistoryResponse> getOrdersByUserIdAndStatus(Long userId, OrderStatus orderStatus) {
        List<OrderHistoryResponse> orders = orderRepository.findOrders(userId, orderStatus);

        List<Long> orderIds = orders.stream()
                .map(OrderHistoryResponse::getOrderId)
                .toList();

        if (orderIds.isEmpty()) {
            return List.of();
        }

        List<OrderItemResponse> items = orderItemRepository.findItems(orderIds);

        Map<Long, Set<OrderItemResponse>> itemMap =
                items.stream()
                        .collect(Collectors.groupingBy(
                                OrderItemResponse::getOrderId,
                                Collectors.toSet()
                        ));

        orders.forEach(order ->
                order.setOrderItems(itemMap.get(order.getOrderId()))
        );

        return orders;
    }*/
}
