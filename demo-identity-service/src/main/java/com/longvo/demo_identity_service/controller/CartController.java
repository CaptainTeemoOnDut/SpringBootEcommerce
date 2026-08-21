package com.longvo.demo_identity_service.controller;


import com.longvo.demo_identity_service.dto.request.CartItemRequest;
import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.service.CartRedisService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartController {

    CartRedisService cartRedisService;

    @PostMapping("/items/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CartItemResponse> addToCart(@Valid @RequestBody CartItemRequest request, @PathVariable Long userId) {

        return ApiResponse.<CartItemResponse>builder()
                .result(cartRedisService.updateCartRedis(userId, request))
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<CartResponse> getCart(@PathVariable Long userId) {
        return ApiResponse.<CartResponse>builder()
                .result(cartRedisService.getUserCart(userId))
                .build();
    }

    /*@PostMapping("/preview")
    public ApiResponse<OrderGroupPreviewResponse> orderPreview(@Valid @RequestBody CartItemRequest cartItemRequest) {
        return ApiResponse.<OrderGroupPreviewResponse>builder()
                .result(orderService.createDraftOrder(cartItemRequest))
                .build();
    }

    @PostMapping
    public ApiResponse<PurchaseResponse> placeOrder(@RequestBody PurchaseRequest purchaseRequest, HttpServletRequest request) {
        return ApiResponse.<PurchaseResponse>builder()
                .result(checkoutService.placeOrder(purchaseRequest, request))
                .build();
    }*/


    /*@PostMapping("/create-payment-intent")
    public ApiResponse<String> createPaymentIntent(@RequestBody PaymentRequest paymentRequest) throws StripeException {

        PaymentIntent paymentIntent = checkoutService.createPaymentIntent(paymentRequest);

        String paymentStr = paymentIntent.toJson();

        return ApiResponse.<String>builder()
                .result(paymentStr)
                .build();
    }*/
}
