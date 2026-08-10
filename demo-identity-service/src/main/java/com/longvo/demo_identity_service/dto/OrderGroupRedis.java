package com.longvo.demo_identity_service.dto;

import com.longvo.demo_identity_service.dto.response.DraftOrderItemRedis;
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
public class OrderGroupRedis {
    Long orderGroupId;

    Long userId;

    BigDecimal totalAmount;

    LocalDateTime expiredAt;

    List<DraftOrderRedis> orders;

}
