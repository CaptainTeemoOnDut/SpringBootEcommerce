package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.request.PaymentRequest;
import com.longvo.demo_identity_service.dto.request.PurchaseRequest;
import com.longvo.demo_identity_service.dto.response.OrderCreationResponse;
import com.longvo.demo_identity_service.dto.response.PurchaseResponse;
import com.longvo.demo_identity_service.entity.Order;
import com.longvo.demo_identity_service.entity.OrderGroup;
import com.longvo.demo_identity_service.entity.OrderItem;
import com.longvo.demo_identity_service.entity.User;
import com.longvo.demo_identity_service.enums.OrderGroupStatus;
import com.longvo.demo_identity_service.enums.OrderStatus;
import com.longvo.demo_identity_service.enums.PaymentMethods;
import com.longvo.demo_identity_service.repository.OrderGroupRepository;
import com.longvo.demo_identity_service.repository.OrderRepository;
import com.longvo.demo_identity_service.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    UserRepository userRepository;
    OrderRepository orderRepository;
    OrderService orderService;
    VNPayService vnPayService;
    OrderGroupRepository orderGroupRepository;

    public CheckoutServiceImpl(UserRepository userRepository,
                               @Value("${stripe.secret.key}") String secretKey, OrderRepository orderRepository, OrderService orderService, VNPayService vnPayService, OrderGroupRepository orderGroupRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.vnPayService = vnPayService;
        this.orderGroupRepository = orderGroupRepository;

        // initialize Stripe Api with secret key
        Stripe.apiKey = secretKey;
    }

    @Transactional
    public boolean processPaymentIpn(
            Long orderGroupId,
            String responseCode,
            Long vnpAmount
    ) {

        OrderGroup orderGroup = orderGroupRepository.findById(orderGroupId)
                .orElseThrow(() -> new RuntimeException("Order group not found"));

        if (orderGroup.getStatus() == OrderGroupStatus.PAID) {
            return true;
        }

        long expectedAmount = Long.parseLong(String.valueOf(orderGroup.getTotalAmount()))  * 100;

        if (expectedAmount != vnpAmount) {
            throw new RuntimeException("Amount mismatch");
        }

        if ("00".equals(responseCode)) {

            orderGroup.setStatus(OrderGroupStatus.PAID);
            orderGroup.setPaidAt(LocalDateTime.now());

            orderGroupRepository.save(orderGroup);

            return true;

        } else {

            orderGroup.setStatus(OrderGroupStatus.FAILED);

            orderGroupRepository.save(orderGroup);

            return true;
        }
    }

    // TAM THOI COMMENT
    /*@Override
    @Transactional
    public PurchaseResponse placeOrder(PurchaseRequest purchaseRequest, HttpServletRequest request) {

        // create an order
        Order order = orderService.createOrder(purchaseRequest);

        if (order == null) {
            throw new RuntimeException("Create order failed");
        }

        PurchaseResponse response = new PurchaseResponse();
        response.setOrderId(order.getId());

        if (order.getPaymentMethod() == PaymentMethods.VNPAY) {
            response.setPaymentUrl(
                    vnPayService.createVnpayUrl(order, request)
            );
        }


        return response;
    }*/

    // PLEASE, DO NOT DELETE
    /*@Override
    @Transactional
    public PurchaseResponse placeOrder(PurchaseRequest purchaseRequest) {

        // retrieve the order info from dto
        Order order = purchaseRequest.getOrder();

        // generate tracking number
        String orderTrackingNumber = generateOrderTrackingNumber();
        order.setOrderTrackingNumber(orderTrackingNumber);

        // populate order with orderItems
        Set<OrderItem> orderItems = order.getOrderItems();
        orderItems.forEach(item -> order.add(item));

        // populate order with billingAddress and ShippingAddress
        order.setBillingAddress(purchaseRequest.getBillingAddress());
        order.setShippingAddress(purchaseRequest.getShippingAddress());

        order.setStatus(OrderStatus.PENDING_PAYMENT);

        // populate user with order
        User user = purchaseRequest.getUser();

        // check if this is an existing user
        String theEmail = user.getEmail();

        User userFromDB = userRepository.findByEmail(theEmail);
        if (userFromDB != null) {
            // we found them ... let's assign them accordingly
            user = userFromDB;
        }
        user.addOrder(order);

        // save to the database
        userRepository.save(user);

        // return a response
        return new PurchaseResponse(orderTrackingNumber);
    }*/

    /*@Override
    public PaymentIntent createPaymentIntent(PaymentRequest paymentRequest) throws StripeException {

        List<String> paymentMethodTypes = new ArrayList<>();
        paymentMethodTypes.add("card");

        Map<String, Object> params = new HashMap<>();
        params.put("amount", paymentRequest.getAmount());
        params.put("currency", paymentRequest.getCurrency());
        params.put("payment_method_types", paymentMethodTypes);
        params.put("description", "Luv2Shop purchase");
        params.put("receipt_email", paymentRequest.getReceiptEmail());

        return PaymentIntent.create(params);
    }*/

    @Transactional
    @Override
    public void markPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Tránh thanh toán trùng
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());

        orderRepository.save(order);

        // (tuỳ chọn)
        // - Trừ tồn kho
        // - Xóa giỏ hàng
        // - Ghi lịch sử thanh toán
    }

    @Transactional
    @Override
    public void markFailed(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Tránh thanh toán trùng
        if (order.getStatus() == OrderStatus.FAILED) {
            return;
        }

        order.setStatus(OrderStatus.FAILED);
        order.setPaidAt(LocalDateTime.now());

        orderRepository.save(order);

        // (tuỳ chọn)
        // - Trừ tồn kho
        // - Xóa giỏ hàng
        // - Ghi lịch sử thanh toán
    }


}
