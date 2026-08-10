package com.longvo.demo_identity_service.dto.request;

import com.longvo.demo_identity_service.entity.Address;
import com.longvo.demo_identity_service.entity.Order;
import com.longvo.demo_identity_service.entity.OrderItem;
import com.longvo.demo_identity_service.entity.User;
import com.longvo.demo_identity_service.enums.PaymentMethods;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseRequest {
    //String userEmail;
    AddressCreationRequest shippingAddress;
    AddressCreationRequest billingAddress;
    private Set<OrderItemRequest> orderItems = new HashSet<>();

    PaymentMethods paymentMethods; //VNPAY COD
}
