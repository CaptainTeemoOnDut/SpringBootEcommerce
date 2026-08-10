package com.longvo.demo_identity_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShopOrderPreview {
    Long shopId;
    String shopName;
    List<OrderItemResponse> items;
    BigDecimal shopSubtotal;
    BigDecimal shopTotal;
    BigDecimal shippingFee;

}

