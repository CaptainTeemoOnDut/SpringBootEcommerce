package com.longvo.demo_identity_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {

    /*int amount;
    String currency;
    String receiptEmail;*/
    String paymentMethod;
    OrderCreationRequest orderCreationRequest;
}
