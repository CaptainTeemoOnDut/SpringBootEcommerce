package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.PaymentRequestDTO;
import com.longvo.demo_identity_service.service.CheckoutService;
import com.longvo.demo_identity_service.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/vnpay")
@RequiredArgsConstructor
public class VNPayController {

    private final VNPayService vnPayService;
    private final CheckoutService checkoutService;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(
            @RequestBody PaymentRequestDTO request,
            HttpServletRequest httpRequest) {

        String paymentUrl = vnPayService.createPaymentUrl(request, httpRequest);

        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/return")
    public ResponseEntity<?> paymentReturn(HttpServletRequest request) {

        Map<String, String> fields = new HashMap<>();

        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {

            String fieldName = params.nextElement();

            String fieldValue = request.getParameter(fieldName);

            fields.put(fieldName, fieldValue);
        }

        String responseCode = request.getParameter("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            return ResponseEntity.ok("Thanh toán thành công");
        }

        return ResponseEntity.ok("Thanh toán thất bại");
    }

    @GetMapping("/ipn")
    public ResponseEntity<String> ipn(HttpServletRequest request) {

        String txnRef = request.getParameter("vnp_TxnRef");

        if (txnRef == null) {
            return ResponseEntity.ok(
                    "{\"RspCode\":\"01\",\"Message\":\"Missing transaction reference\"}"
            );
        }

        String[] parts = txnRef.split("_");

        Long orderGroupId = Long.valueOf(parts[1]);

        String responseCode = request.getParameter("vnp_ResponseCode");

        String vnpAmount = request.getParameter("vnp_Amount");

        if ("00".equals(responseCode)) {

            // cập nhật order thành PAID
            checkoutService.processPaymentIpn(orderGroupId, responseCode, Long.valueOf(vnpAmount));
            return ResponseEntity.ok("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
        }

        return ResponseEntity.ok("{\"RspCode\":\"01\",\"Message\":\"Order Fail\"}");
    }

}
