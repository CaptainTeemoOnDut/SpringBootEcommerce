package com.longvo.demo_identity_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {

    private Long orderGroupId;
    private BigDecimal amount;
    private String orderInfo;
}
