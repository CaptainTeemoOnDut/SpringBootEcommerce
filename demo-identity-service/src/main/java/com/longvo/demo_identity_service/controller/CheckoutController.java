package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.*;
import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.service.CheckoutService;
import com.longvo.demo_identity_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CheckoutController {

    CheckoutService checkoutService;
    OrderService orderService;

    @PostMapping("/preview")
    public ApiResponse<OrderGroupPreviewResponse> orderPreview(@Valid @RequestBody CartRequest request) {
        return ApiResponse.<OrderGroupPreviewResponse>builder()
                .result(orderService.previewOrder(request))
                .build();
    }

    @PostMapping
    public ApiResponse<OrderGroupResponse> createDraftOrder(@RequestBody PlaceOrderRequest placeOrderRequest, HttpServletRequest httpServletRequest) throws UnsupportedEncodingException {
        //String clientIp = httpServletRequest.getRemoteAddr();
        return ApiResponse.<OrderGroupResponse>builder()
                .result(orderService.createDraftOrder(placeOrderRequest, httpServletRequest))
                .build();
    }

    //TAM THOI COMMENT
    /*@PostMapping
    public ApiResponse<OrderGroupResponse> placeOrder(@RequestBody PlaceOrderRequest placeOrderRequest) {
        return ApiResponse.<OrderGroupResponse>builder()
                .result(orderService.placeOrder(placeOrderRequest))
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
