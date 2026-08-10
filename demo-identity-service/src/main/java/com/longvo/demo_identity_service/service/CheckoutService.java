package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.request.PaymentRequest;
import com.longvo.demo_identity_service.dto.request.PurchaseRequest;
import com.longvo.demo_identity_service.dto.response.PurchaseResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.servlet.http.HttpServletRequest;

public interface CheckoutService {
    //PurchaseResponse placeOrder(PurchaseRequest purchaseRequest, HttpServletRequest request);

    //PaymentIntent createPaymentIntent(PaymentRequest paymentRequest) throws StripeException;

    void markPaid(Long orderGroupId);

    void markFailed(Long orderGroupId);

    boolean processPaymentIpn(Long aLong, String responseCode, Long aLong1);
}
