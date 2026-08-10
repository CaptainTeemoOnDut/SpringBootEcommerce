package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.PaymentRequest;
import com.longvo.demo_identity_service.dto.request.PurchaseRequest;
import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.PurchaseResponse;
import com.longvo.demo_identity_service.service.CheckoutService;
import com.longvo.demo_identity_service.service.VNPayService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VNPayController {


    /*VNPayService vnPayService;
    CheckoutService checkoutService;
    @GetMapping("/callback")
    public void handleCallback(HttpServletResponse response) throws IOException {
        response.sendRedirect(
                "http://localhost:4200/payment-result?status=success"
        );
    }

    // Instant Payment Notification (IPN)
    @GetMapping("/ipn")
    public ResponseEntity<String> handleIpn(HttpServletRequest request) {

        Map<String, String> params = extractParams(request);

        boolean isValid = vnPayService.verifySignature(params);

        if (!isValid) {
            return ResponseEntity.ok("97"); // invalid signature
        }

        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String amount = params.get("vnp_Amount");

        try {

            boolean success = checkoutService.processPaymentIpn(
                    Long.valueOf(orderId),
                    responseCode,
                    Long.valueOf(amount)
            );

            if (success) {
                return ResponseEntity.ok("00");
            } else {
                return ResponseEntity.ok("01");
            }

        } catch (Exception e) {
            return ResponseEntity.ok("99");
        }
    }


    private Map<String, String> extractParams(HttpServletRequest request) {

        Map<String, String> params = new HashMap<>();

        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);

            if (paramValue != null && !paramValue.isEmpty()) {
                params.put(paramName, paramValue);
            }
        }

        return params;
    }*/




}
