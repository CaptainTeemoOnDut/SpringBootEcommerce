package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.OrderGroupHistoryResponse;
import com.longvo.demo_identity_service.dto.response.OrderHistoryResponse;
import com.longvo.demo_identity_service.dto.response.PagedResponse;
import com.longvo.demo_identity_service.enums.OrderGroupStatus;
import com.longvo.demo_identity_service.enums.OrderStatus;
import com.longvo.demo_identity_service.service.OrderGroupHistoryService;
import com.longvo.demo_identity_service.service.OrderHistoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderHistoryController {
    OrderHistoryService orderHistoryService;
    OrderGroupHistoryService orderGroupHistoryService;

    @GetMapping("/history")
    public Page<OrderGroupHistoryResponse> getOrderHistory(
            @RequestParam Long userId,
            @RequestParam(name = "status", required = false) OrderGroupStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size

    ) {
        System.out.println("STATUS = " + status);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        return orderGroupHistoryService.getOrderHistory(userId, status, pageable);
    }

    /*@GetMapping("/history")
    public ApiResponse<List<OrderHistoryResponse>> getOrderHistory(
            @RequestParam Long userId,
            @RequestParam OrderStatus orderStatus
    ) {

        return ApiResponse.<List<OrderHistoryResponse>>builder()
                .result(orderHistoryService.getOrdersByUserIdAndStatus(userId, orderStatus))
                .build();

    }*/

    /*@GetMapping("/history")
    public ApiResponse<PagedResponse<OrderHistoryResponse>> getOrderHistory(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderHistoryResponse> orderHistoryResponsePage = orderHistoryService.getOrdersByUserEmail(email, pageable);
        return getPagedResponseApiResponse(orderHistoryResponsePage);
    }

    private ApiResponse<PagedResponse<OrderHistoryResponse>> getPagedResponseApiResponse(Page<OrderHistoryResponse> orderHistoryResponsePage) {
        PagedResponse<OrderHistoryResponse> response = PagedResponse.<OrderHistoryResponse>builder()
                .content(orderHistoryResponsePage.getContent())
                .page(orderHistoryResponsePage.getNumber())
                .size(orderHistoryResponsePage.getSize())
                .totalElements(orderHistoryResponsePage.getTotalElements())
                .totalPages(orderHistoryResponsePage.getTotalPages())
                .last(orderHistoryResponsePage.isLast())
                .build();
        return ApiResponse.<PagedResponse<OrderHistoryResponse>>builder()
                .result(response)
                .build();
    }*/
}


