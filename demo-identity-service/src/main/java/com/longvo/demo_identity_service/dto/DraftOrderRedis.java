package com.longvo.demo_identity_service.dto;

import com.longvo.demo_identity_service.dto.response.DraftOrderItemRedis;
import com.longvo.demo_identity_service.enums.AttributeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DraftOrderRedis {
    private Long orderId;
    private Long shopId;

    private List<DraftOrderItemRedis> items;

}
