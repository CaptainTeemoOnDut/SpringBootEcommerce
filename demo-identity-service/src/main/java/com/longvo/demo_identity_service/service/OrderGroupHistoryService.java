package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.response.OrderGroupHistoryResponse;
import com.longvo.demo_identity_service.dto.response.OrderHistoryResponse;
import com.longvo.demo_identity_service.dto.response.OrderItemResponse;
import com.longvo.demo_identity_service.enums.OrderGroupStatus;
import com.longvo.demo_identity_service.repository.OrderGroupRepository;
import com.longvo.demo_identity_service.repository.OrderItemRepository;
import com.longvo.demo_identity_service.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderGroupHistoryService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderGroupRepository orderGroupRepository;

    public Page<OrderGroupHistoryResponse> getOrderHistory(Long userId, OrderGroupStatus orderStatus, Pageable pageable) {
        Page<OrderGroupHistoryResponse> orderGroupPage;
        if (orderStatus != null) {
            orderGroupPage = orderGroupRepository.findOrderGroups(userId, orderStatus, pageable);
        } else {
            orderGroupPage = orderGroupRepository.findOrderGroupsByUserId(userId, pageable);
        }

        List<OrderGroupHistoryResponse> orderGroups = orderGroupPage.getContent();

        List<Long> orderGroupIds = orderGroups.stream()
                .map(OrderGroupHistoryResponse::getOrderGroupId)
                .toList();

        if (orderGroupIds.isEmpty()) {
            return orderGroupPage;
        }

        List<OrderHistoryResponse> orders =
                orderRepository.findOrdersByOrderGroupIds(orderGroupIds);

        Map<Long, Set<OrderHistoryResponse>> orderGroupMap =
                orders.stream()
                        .collect(Collectors.groupingBy(
                                OrderHistoryResponse::getOrderGroupId,
                                Collectors.toCollection(LinkedHashSet::new)
                        ));

        List<Long> orderIds = orders.stream()
                .map(OrderHistoryResponse::getOrderId)
                .toList();

        if (!orderIds.isEmpty()) {

            List<OrderItemResponse> items =
                    orderItemRepository.findItems(orderIds);

            Map<Long, Set<OrderItemResponse>> orderItemMap =
                    items.stream()
                            .collect(Collectors.groupingBy(
                                    OrderItemResponse::getOrderId,
                                    Collectors.toCollection(LinkedHashSet::new)
                            ));

            orders.forEach(order ->
                    order.setOrderItems(
                            orderItemMap.getOrDefault(order.getOrderId(), Set.of())
                    )
            );
        }

        orderGroups.forEach(group ->
                group.setOrderHistoryResponses(
                        orderGroupMap.getOrDefault(group.getOrderGroupId(), Set.of())
                )
        );

        return orderGroupPage;
    }

    /*public List<OrderGroupHistoryResponse> getOrderGroupByUserIdAndStatus(Long userId, OrderStatus orderStatus) {
        List<OrderGroupHistoryResponse> orderGroups = orderGroupRepository.findOrderGroup(userId, orderStatus);

        List<Long> orderGroupIds = orderGroups.stream()
                .map(OrderGroupHistoryResponse::getOrderGroupId)
                .toList();

        if (orderGroupIds.isEmpty()) {
            return List.of();
        }

        List<OrderHistoryResponse> orders = orderRepository.findOrdersByOrderGroupIds(orderGroupIds);
        Map<Long, Set<OrderHistoryResponse>> orderGroupMap =
                orders.stream()
                        .collect(Collectors.groupingBy(
                                OrderHistoryResponse::getOrderGroupId,
                                Collectors.toCollection(LinkedHashSet::new)
                        ));


        List<Long> orderIds = orders.stream()
                .map(OrderHistoryResponse::getOrderId)
                .toList();

        if (orderIds.isEmpty()) {
            return List.of();
        }

        List<OrderItemResponse> items = orderItemRepository.findItems(orderIds);

        Map<Long, Set<OrderItemResponse>> orderMap =
                items.stream()
                        .collect(Collectors.groupingBy(
                                OrderItemResponse::getOrderId,
                                Collectors.toSet()
                        ));

        orders.forEach(order ->
                order.setOrderItems(orderMap.get(order.getOrderId()))
        );

        orderGroups.forEach(orderGroup ->
                orderGroup.setOrderHistoryResponses(
                        orderGroupMap.getOrDefault(orderGroup.getOrderGroupId(), Set.of())
                )
        );

        return orderGroups;
    }*/
}
