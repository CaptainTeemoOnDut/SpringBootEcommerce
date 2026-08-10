package com.longvo.demo_identity_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.longvo.demo_identity_service.entity.Address;
import com.longvo.demo_identity_service.entity.OrderItem;
import com.longvo.demo_identity_service.entity.User;
import com.longvo.demo_identity_service.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderHistoryResponse {
    //private String orderTrackingNumber;
    Long orderId;

    Long orderGroupId;

    ShopResponse shopResponse;

    Set<OrderItemResponse> orderItems;


    BigDecimal totalPrice;


    OrderStatus status;

    LocalDateTime dateCreated;

    public OrderHistoryResponse(Long orderId, Long orderGroupId, String shopName, BigDecimal totalPrice, OrderStatus status, LocalDateTime dateCreated) {
        this.orderId = orderId;
        this.orderGroupId = orderGroupId;
        this.shopResponse = new ShopResponse(shopName);
        this.totalPrice = totalPrice;
        this.status = status;
        this.dateCreated = dateCreated;
    }

    //private Date lastUpdated;





    //@JsonIgnore
    //private User user;


    //private Address shippingAddress;


    //private Address billingAddress;
}
