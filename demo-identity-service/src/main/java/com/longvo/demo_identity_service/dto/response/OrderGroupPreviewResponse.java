package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.enums.PaymentMethods;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderGroupPreviewResponse {
    //Long orderGroupId;
    List<ShopOrderPreview> shops;
    //Long addressId;
    String fullAddress;
    String receiverName;
    String phoneNumber;
    BigDecimal subtotalBeforeDiscount;
    BigDecimal discountAmount;
    BigDecimal grandTotal;
    PaymentMethods paymentMethod;
    //LocalDateTime expiredAt;
}
